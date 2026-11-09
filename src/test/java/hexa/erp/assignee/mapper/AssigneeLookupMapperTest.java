package hexa.erp.assignee.mapper;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.StringUtils;

import hexa.erp.assignee.domain.AssigneeLookupVO;
import hexa.erp.common.domain.LookupCriteria;

/** Oracle接続なしでMyBatis XML・動的SQL・Bean定義を検証する。SQL実行はITで検証する。 */
public class AssigneeLookupMapperTest {
	private Configuration configuration;
	private static final String NAMESPACE = "hexa.erp.assignee.mapper.AssigneeLookupMapper";

	@Before
	public void loadMapperWithoutOpeningConnection() throws Exception {
		SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
		factory.setDataSource(new DriverManagerDataSource());
		factory.setConfigLocation(new ClassPathResource("mybatis-config.xml"));
		factory.setMapperLocations(
				new Resource[] { new ClassPathResource("hexa/erp/assignee/mapper/AssigneeLookupMapper.xml") });
		configuration = factory.getObject().getConfiguration();
	}

	@Test
	public void statementIdsAndResultTypeMatchMapperInterface() {
		assertTrue(configuration.hasMapper(AssigneeLookupMapper.class));
		Set<String> methods = new HashSet<>();
		for (java.lang.reflect.Method method : AssigneeLookupMapper.class.getDeclaredMethods()) {
			methods.add(method.getName());
		}
		for (String id : Arrays.asList("getListWithPaging", "getTotalCount", "read")) {
			assertTrue(methods.contains(id));
			assertEquals(SqlCommandType.SELECT, statement(id).getSqlCommandType());
		}
		assertEquals(AssigneeLookupVO.class, statement("getListWithPaging").getResultMaps().get(0).getType());
		assertEquals(AssigneeLookupVO.class, statement("read").getResultMaps().get(0).getType());
		assertEquals(Integer.class, statement("getTotalCount").getResultMaps().get(0).getType());
	}

	@Test
	public void camelCaseFieldsAndNullPolicyRemainConfigured() {
		assertTrue(configuration.isMapUnderscoreToCamelCase());
		assertEquals(JdbcType.NULL, configuration.getJdbcTypeForNull());
		MetaClass fields = MetaClass.forClass(AssigneeLookupVO.class, new DefaultReflectorFactory());
		assertEquals("assigneeId", fields.findProperty("ASSIGNEE_ID", true));
		assertEquals("assigneeCode", fields.findProperty("ASSIGNEE_CODE", true));
		assertEquals("assigneeName", fields.findProperty("ASSIGNEE_NAME", true));
		assertEquals("activeFlag", fields.findProperty("ACTIVE_FLAG", true));
		assertEquals(Long.class, fields.getGetterType("assigneeId"));
		assertEquals(String.class, fields.getGetterType("assigneeCode"));
		assertFalse(fields.hasGetter("rn"));
	}

	@Test
	public void emptyKeywordKeepsOnlyActiveConditionAndOracle11gBounds() {
		LookupCriteria criteria = new LookupCriteria();
		for (String keyword : Arrays.asList(null, "", "  ")) {
			criteria.setKeyword(keyword);
			BoundSql list = statement("getListWithPaging").getBoundSql(criteria);
			String sql = compact(list);
			assertTrue(sql.contains("FROM ASSIGNEE WHERE ACTIVE_FLAG = 'Y' ORDER BY ASSIGNEE_CODE ASC"));
			assertTrue(sql.contains("SELECT ROWNUM rn, ordered_assignee.*"));
			assertTrue(sql.contains("WHERE ROWNUM <= ? * ?"));
			assertTrue(sql.contains("WHERE rn > (? - 1) * ?"));
			assertTrue(sql.endsWith("ORDER BY ASSIGNEE_CODE ASC"));
			assertFalse(sql.contains("LIKE") || sql.contains("OFFSET") || sql.contains("FETCH"));
			assertEquals(Arrays.asList("pageNum", "amount", "pageNum", "amount"), parameters(list));
			BoundSql total = statement("getTotalCount").getBoundSql(criteria);
			assertEquals("SELECT COUNT(*) FROM ASSIGNEE WHERE ACTIVE_FLAG = 'Y'", compact(total));
			assertTrue(total.getParameterMappings().isEmpty());
		}
	}

	@Test
	public void listAndTotalBindTheSameKeywordInsteadOfInterpolatingSql() {
		LookupCriteria criteria = new LookupCriteria();
		criteria.setPageNum(2);
		criteria.setKeyword("  00001' OR 1=1 -- %_  ");
		BoundSql list = statement("getListWithPaging").getBoundSql(criteria);
		BoundSql total = statement("getTotalCount").getBoundSql(criteria);
		String condition = "WHERE ACTIVE_FLAG = 'Y' AND ( UPPER(ASSIGNEE_CODE) LIKE '%' || UPPER(?) || '%' "
				+ "OR UPPER(ASSIGNEE_NAME) LIKE '%' || UPPER(?) || '%' )";
		assertTrue(compact(list).contains(condition));
		assertTrue(compact(total).endsWith(condition));
		assertFalse(compact(list).contains(criteria.getKeyword()));
		assertFalse(compact(total).contains(criteria.getKeyword()));
		assertEquals(Arrays.asList("keyword", "keyword", "pageNum", "amount", "pageNum", "amount"), parameters(list));
		assertEquals(Arrays.asList("keyword", "keyword"), parameters(total));
		assertFalse(compact(total).contains("ROWNUM") || compact(total).contains("ORDER BY"));
	}

	@Test
	public void actualRootKeepsAssigneeInfrastructureWithBusinessScans() {
		// refresh/getBeanを呼ばずBean定義のみ読み込み、実際の接続プールを起動させない。
		try (GenericApplicationContext root = new GenericApplicationContext()) {
			new XmlBeanDefinitionReader(root)
					.loadBeanDefinitions("file:src/main/webapp/WEB-INF/spring/root-context.xml");
			assertEquals(0, root.getEnvironment().getActiveProfiles().length);
			assertFalse(root.isActive());

			BeanDefinition hikari = root.getBeanDefinition("hikariConfig");
			assertEquals("com.zaxxer.hikari.HikariConfig", hikari.getBeanClassName());
			assertEquals("${db.driver}", value(hikari.getPropertyValues().get("driverClassName")));
			assertEquals("${db.url}", value(hikari.getPropertyValues().get("jdbcUrl")));
			assertEquals("${db.username}", value(hikari.getPropertyValues().get("username")));
			assertEquals("${db.password}", value(hikari.getPropertyValues().get("password")));
			assertEquals("5", value(hikari.getPropertyValues().get("maximumPoolSize")));
			assertEquals("1", value(hikari.getPropertyValues().get("minimumIdle")));
			assertEquals("hexa-erp-db", value(hikari.getPropertyValues().get("poolName")));

			BeanDefinition source = root.getBeanDefinition("dataSource");
			assertEquals("com.zaxxer.hikari.HikariDataSource", source.getBeanClassName());
			assertFalse(source.isLazyInit());
			assertEquals("hikariConfig",
					reference(source.getConstructorArgumentValues().getGenericArgumentValues().get(0).getValue()));

			BeanDefinition factory = root.getBeanDefinition("sqlSessionFactory");
			assertEquals("org.mybatis.spring.SqlSessionFactoryBean", factory.getBeanClassName());
			assertEquals("dataSource", reference(factory.getPropertyValues().get("dataSource")));
			assertEquals("classpath:mybatis-config.xml", value(factory.getPropertyValues().get("configLocation")));
			assertEquals("classpath*:hexa/erp/*/mapper/*Mapper.xml",
					value(factory.getPropertyValues().get("mapperLocations")));

			BeanDefinition transaction = root.getBeanDefinition("transactionManager");
			assertEquals("org.springframework.jdbc.datasource.DataSourceTransactionManager",
					transaction.getBeanClassName());
			assertEquals("dataSource", reference(transaction.getPropertyValues().get("dataSource")));
			assertEquals("hexa.erp.assignee.service.AssigneeLookupServiceImpl",
					root.getBeanDefinition("assigneeLookupServiceImpl").getBeanClassName());

			// MyBatis-Spring 1.3.2のXML <scan>はMapperFactoryBeanを直接登録する。
			BeanDefinition mapper = root.getBeanDefinition("assigneeLookupMapper");
			assertEquals("org.mybatis.spring.mapper.MapperFactoryBean", mapper.getBeanClassName());
			assertEquals(NAMESPACE,
					value(mapper.getConstructorArgumentValues().getGenericArgumentValues().get(0).getValue()));
			assertEquals("sqlSessionFactory", reference(mapper.getPropertyValues().get("sqlSessionFactory")));

			int placeholders = 0;
			for (String name : root.getBeanDefinitionNames()) {
				BeanDefinition definition = root.getBeanDefinition(name);
				if ("org.springframework.context.support.PropertySourcesPlaceholderConfigurer"
						.equals(definition.getBeanClassName())) {
					assertArrayEquals(new String[] { "classpath:db.properties", "classpath:upload.properties" },
							StringUtils.trimArrayElements((String[]) definition.getPropertyValues().get("locations")));
					assertEquals(Boolean.FALSE, definition.getPropertyValues().get("ignoreResourceNotFound"));
					placeholders++;
				}
			}
			assertEquals(1, placeholders);
			assertFalse(root.getBeanFactory().containsSingleton("dataSource"));
		}
	}

	@Test
	public void classpathPropertiesProvideAllConnectionKeys() throws Exception {
		// 接続情報は読み取りのみとし、ログに出力しない。
		Properties properties = new Properties();
		try (InputStream input = new ClassPathResource("db.properties").getInputStream()) {
			properties.load(input);
		}
		assertEquals(new HashSet<>(Arrays.asList("db.driver", "db.url", "db.username", "db.password")),
				properties.stringPropertyNames());
		for (String key : properties.stringPropertyNames()) {
			assertFalse(key + " の設定が空になっている。", properties.getProperty(key).trim().isEmpty());
		}
	}

	private String reference(Object value) {
		assertTrue(value instanceof RuntimeBeanReference);
		return ((RuntimeBeanReference) value).getBeanName();
	}

	@Test
	public void selectedIdReadBindsTheIdAndIncludesInactiveRows() {
		BoundSql read = statement("read").getBoundSql(9007199254740993L);
		String sql = compact(read);
		assertTrue(sql.contains("ASSIGNEE_ID, ASSIGNEE_CODE, ASSIGNEE_NAME"));
		assertTrue(sql.contains("FROM ASSIGNEE"));
		assertTrue(sql.endsWith("WHERE ASSIGNEE_ID = ?"));
		assertEquals(Arrays.asList("assigneeId"), parameters(read));
		assertFalse(sql.contains("9007199254740993"));
		assertFalse(sql.contains("ACTIVE_FLAG =") || sql.contains("ROWNUM") || sql.contains("LIKE"));
	}

	private MappedStatement statement(String id) {
		return configuration.getMappedStatement(NAMESPACE + "." + id);
	}

	private String compact(BoundSql sql) {
		return sql.getSql().replaceAll("\\s+", " ").trim();
	}

	private List<String> parameters(BoundSql sql) {
		List<String> result = new ArrayList<>();
		for (ParameterMapping parameter : sql.getParameterMappings()) {
			result.add(parameter.getProperty());
		}
		return result;
	}

	private String value(Object value) {
		assertNotNull(value);
		return value instanceof TypedStringValue ? ((TypedStringValue) value).getValue() : value.toString();
	}
}
