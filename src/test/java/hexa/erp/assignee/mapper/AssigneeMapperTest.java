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

import hexa.erp.assignee.domain.AssigneeVO;
import hexa.erp.common.domain.LookupCriteria;

/** Oracle 연결 없이 MyBatis XML·동적 SQL·빈 정의를 검사한다. SQL 실행 검사는 IT에서 한다. */
public class AssigneeMapperTest {
	private Configuration configuration;
	private static final String NAMESPACE = "hexa.erp.assignee.mapper.AssigneeMapper";

	@Before
	public void loadMapperWithoutOpeningConnection() throws Exception {
		SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
		factory.setDataSource(new DriverManagerDataSource());
		factory.setConfigLocation(new ClassPathResource("mybatis-config.xml"));
		factory.setMapperLocations(new Resource[] {
				new ClassPathResource("hexa/erp/assignee/mapper/AssigneeMapper.xml") });
		configuration = factory.getObject().getConfiguration();
	}

	@Test
	public void statementIdsAndResultTypeMatchMapperInterface() {
		assertTrue(configuration.hasMapper(AssigneeMapper.class));
		Set<String> statementNames = new HashSet<>();
		for (String name : configuration.getMappedStatementNames()) {
			if (name.startsWith(NAMESPACE + ".")) {
				statementNames.add(name.substring(NAMESPACE.length() + 1));
				assertEquals(SqlCommandType.SELECT, configuration.getMappedStatement(name).getSqlCommandType());
			}
		}
		assertEquals(new HashSet<>(Arrays.asList("getList", "getTotal")), statementNames);
		Set<String> methods = new HashSet<>();
		for (java.lang.reflect.Method method : AssigneeMapper.class.getDeclaredMethods()) {
			methods.add(method.getName());
		}
		assertEquals(methods, statementNames);
		assertEquals(AssigneeVO.class, statement("getList").getResultMaps().get(0).getType());
		assertEquals(Integer.class, statement("getTotal").getResultMaps().get(0).getType());
	}

	@Test
	public void camelCaseFieldsAndNullPolicyRemainConfigured() {
		assertTrue(configuration.isMapUnderscoreToCamelCase());
		assertEquals(JdbcType.NULL, configuration.getJdbcTypeForNull());
		MetaClass fields = MetaClass.forClass(AssigneeVO.class, new DefaultReflectorFactory());
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
			BoundSql list = statement("getList").getBoundSql(criteria);
			String sql = compact(list);
			assertTrue(sql.contains("FROM ASSIGNEE WHERE ACTIVE_FLAG = 'Y' ORDER BY ASSIGNEE_CODE ASC"));
			assertTrue(sql.contains("SELECT ROWNUM rn, ordered_assignee.*"));
			assertTrue(sql.contains("WHERE ROWNUM <= ? * ?"));
			assertTrue(sql.contains("WHERE rn > (? - 1) * ?"));
			assertTrue(sql.endsWith("ORDER BY ASSIGNEE_CODE ASC"));
			assertFalse(sql.contains("LIKE") || sql.contains("OFFSET") || sql.contains("FETCH"));
			assertEquals(Arrays.asList("pageNum", "amount", "pageNum", "amount"), parameters(list));
			BoundSql total = statement("getTotal").getBoundSql(criteria);
			assertEquals("SELECT COUNT(*) FROM ASSIGNEE WHERE ACTIVE_FLAG = 'Y'", compact(total));
			assertTrue(total.getParameterMappings().isEmpty());
		}
	}

	@Test
	public void listAndTotalBindTheSameKeywordInsteadOfInterpolatingSql() {
		LookupCriteria criteria = new LookupCriteria();
		criteria.setPageNum(2);
		criteria.setKeyword("  00001' OR 1=1 -- %_  ");
		BoundSql list = statement("getList").getBoundSql(criteria);
		BoundSql total = statement("getTotal").getBoundSql(criteria);
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
		// refresh/getBean 없이 빈 정의만 읽어 실제 연결 풀의 시작을 막는다.
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
			assertEquals("hikariConfig", reference(source.getConstructorArgumentValues()
					.getGenericArgumentValues().get(0).getValue()));

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
			assertEquals("hexa.erp.assignee.service.AssigneeServiceImpl",
					root.getBeanDefinition("assigneeServiceImpl").getBeanClassName());

			// MyBatis-Spring 1.3.2의 XML <scan>은 MapperFactoryBean을 직접 등록한다.
			BeanDefinition mapper = root.getBeanDefinition("assigneeMapper");
			assertEquals("org.mybatis.spring.mapper.MapperFactoryBean", mapper.getBeanClassName());
			assertEquals(NAMESPACE, value(mapper.getConstructorArgumentValues()
					.getGenericArgumentValues().get(0).getValue()));
			assertEquals("sqlSessionFactory", reference(mapper.getPropertyValues().get("sqlSessionFactory")));

			int placeholders = 0;
			for (String name : root.getBeanDefinitionNames()) {
				BeanDefinition definition = root.getBeanDefinition(name);
				if ("org.springframework.context.support.PropertySourcesPlaceholderConfigurer"
						.equals(definition.getBeanClassName())) {
					assertArrayEquals(new String[] { "classpath:db.properties" },
							(String[]) definition.getPropertyValues().get("locations"));
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
		// 접속정보는 읽기만 하고 로그에 출력하지 않는다.
		Properties properties = new Properties();
		try (InputStream input = new ClassPathResource("db.properties").getInputStream()) {
			properties.load(input);
		}
		assertEquals(new HashSet<>(Arrays.asList("db.driver", "db.url", "db.username", "db.password")),
				properties.stringPropertyNames());
		for (String key : properties.stringPropertyNames()) {
			assertFalse(key + " 설정이 비어 있다.", properties.getProperty(key).trim().isEmpty());
		}
	}

	private String reference(Object value) {
		assertTrue(value instanceof RuntimeBeanReference);
		return ((RuntimeBeanReference) value).getBeanName();
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
