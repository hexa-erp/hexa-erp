package hexa.erp.testinfra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.junit.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/** Oracle 연결 없이 테스트 Mapper/XML을 검사한다. 실제 연결 확인은 DatabaseConnectionIT에서 한다. */
public class ConnectionProbeConfigurationTest {
	@Test
	public void factoryWithoutTestRegistrationHasNoProbeMapper() throws Exception {
		SqlSessionFactory factory = sessionFactoryWithoutConnection();
		assertFalse(factory.getConfiguration().hasMapper(ConnectionProbeMapper.class));
		assertFalse(factory.getConfiguration().hasStatement(statementId()));
	}

	@Test
	public void testContextLoadsXmlBesideMapperInterface() throws Exception {
		SqlSessionFactory factory = sessionFactoryWithoutConnection();
		try (GenericApplicationContext context = new GenericApplicationContext()) {
			context.setAllowBeanDefinitionOverriding(false);
			context.getBeanFactory().registerSingleton("sqlSessionFactory", factory);
			new XmlBeanDefinitionReader(context).loadBeanDefinitions("classpath:spring/database-test-context.xml");
			context.refresh();

			assertNotNull(context.getBean(ConnectionProbeMapper.class));
			assertEquals(1, context.getBeansOfType(ConnectionProbeMapper.class).size());
			assertTrue(factory.getConfiguration().hasMapper(ConnectionProbeMapper.class));
			assertTrue(factory.getConfiguration().isResourceLoaded("hexa/erp/testinfra/ConnectionProbeMapper.xml"));
			assertTrue(factory.getConfiguration().isMapUnderscoreToCamelCase());
			assertEquals(JdbcType.NULL, factory.getConfiguration().getJdbcTypeForNull());

			MappedStatement statement = factory.getConfiguration().getMappedStatement(statementId());
			assertEquals(ConnectionProbeRow.class, statement.getResultMaps().get(0).getType());
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("probeValue", "configuration-only");
			parameters.put("optionalNote", null);
			BoundSql sql = statement.getBoundSql(parameters);
			assertEquals(
					"SELECT CAST(? AS VARCHAR2(30)) AS PROBE_VALUE, "
							+ "CAST(? AS VARCHAR2(30)) AS OPTIONAL_NOTE FROM DUAL",
					sql.getSql().replaceAll("\\s+", " ").trim());
			assertEquals(2, sql.getParameterMappings().size());
			assertEquals("probeValue", sql.getParameterMappings().get(0).getProperty());
			assertEquals("optionalNote", sql.getParameterMappings().get(1).getProperty());
		}
	}

	private SqlSessionFactory sessionFactoryWithoutConnection() throws Exception {
		SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
		factory.setDataSource(new DriverManagerDataSource());
		factory.setConfigLocation(new ClassPathResource("mybatis-config.xml"));
		return factory.getObject();
	}

	private String statementId() {
		return ConnectionProbeMapper.class.getName() + ".selectProbe";
	}
}
