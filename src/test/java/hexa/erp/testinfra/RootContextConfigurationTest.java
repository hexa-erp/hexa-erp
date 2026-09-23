package hexa.erp.testinfra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Test;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import hexa.erp.assignee.mapper.AssigneeMapper;
import hexa.erp.assignee.service.AssigneeService;
import hexa.erp.assignee.service.AssigneeServiceImpl;

/** Oracle 없이 업무별 스캔·XML 로딩·빈 주입을 검사한다. 테스트 DataSource는 연결을 금지한다. */
public class RootContextConfigurationTest {
	private static final String ROOT_XML = "src/main/webapp/WEB-INF/spring/root-context.xml";
	private static final String MAPPER_PATTERN = "classpath*:hexa/erp/*/mapper/*Mapper.xml";
	private static final String[] BUSINESS_PACKAGES = {
			"assignee", "partner", "warehouse", "item", "quotation", "salesorder",
			"sale", "shipinstruction", "shipment", "stock", "common" };

	@Test
	public void scanDeclarationsListOnlyApprovedMapperAndServicePackages() throws Exception {
		DocumentBuilderFactory builder = DocumentBuilderFactory.newInstance();
		builder.setNamespaceAware(true);
		builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		Document xml = builder.newDocumentBuilder().parse(new File(ROOT_XML));

		NodeList mapperScans = xml.getElementsByTagNameNS("http://mybatis.org/schema/mybatis-spring", "scan");
		NodeList serviceScans = xml.getElementsByTagNameNS("http://www.springframework.org/schema/context", "component-scan");
		assertEquals(expectedPackages("mapper"), declaredPackages(mapperScans));
		assertEquals(expectedPackages("service"), declaredPackages(serviceScans));
		for (int i = 0; i < mapperScans.getLength(); i++) {
			Element scan = (Element) mapperScans.item(i);
			assertEquals("sqlSessionFactory", scan.getAttribute("factory-ref"));
			assertFalse(scan.hasAttribute("annotation"));
			assertFalse(scan.hasAttribute("marker-interface"));
		}
	}

	@Test
	public void wildcardFindsAssigneeXmlButExcludesVisibleConnectionProbeXml() throws Exception {
		try (GenericApplicationContext root = new GenericApplicationContext()) {
			loadRootDefinitions(root);
			Object configured = root.getBeanDefinition("sqlSessionFactory")
					.getPropertyValues().get("mapperLocations");
			String pattern = configured instanceof TypedStringValue
					? ((TypedStringValue) configured).getValue() : configured.toString();
			assertEquals(MAPPER_PATTERN, pattern);

			Set<String> locations = new HashSet<>();
			for (Resource resource : new PathMatchingResourcePatternResolver().getResources(pattern)) {
				assertTrue(resource.exists());
				assertFalse(resource.getURL().toExternalForm().contains("/testinfra/"));
				locations.add(resource.getURL().toExternalForm());
			}
			assertTrue(locations.contains(new ClassPathResource(
					"hexa/erp/assignee/mapper/AssigneeMapper.xml").getURL().toExternalForm()));

			// 테스트 XML이 있어도 업무 Mapper 검색에는 포함되면 안 된다.
			Resource probe = new ClassPathResource("hexa/erp/testinfra/ConnectionProbeMapper.xml");
			assertTrue(probe.exists());
			assertFalse(locations.contains(probe.getURL().toExternalForm()));
			assertFalse(root.isActive());
			assertFalse(root.getBeanFactory().containsSingleton("dataSource"));
		}
	}

	@Test
	public void actualRootRegistersAssigneeGraphWithoutOpeningAnyConnection() {
		NoConnectionDataSource source = new NoConnectionDataSource();
		try (GenericApplicationContext root = new GenericApplicationContext()) {
			root.setAllowBeanDefinitionOverriding(false);
			loadRootDefinitions(root);

			// 테스트 컨텍스트에서만 실제 연결 풀 생성을 막는다.
			root.removeBeanDefinition("dataSource");
			root.removeBeanDefinition("hikariConfig");
			root.getBeanFactory().registerSingleton("dataSource", source);
			root.refresh();

			assertTrue(root.isActive());
			AssigneeMapper mapper = root.getBean(AssigneeMapper.class);
			AssigneeService service = root.getBean(AssigneeService.class);
			assertEquals(1, root.getBeansOfType(AssigneeMapper.class).size());
			assertEquals(1, root.getBeansOfType(AssigneeService.class).size());
			assertTrue(service instanceof AssigneeServiceImpl);
			assertTrue(AssigneeServiceImpl.class.isAnnotationPresent(Service.class));
			assertFalse(AssigneeService.class.isAnnotationPresent(Service.class));
			assertSame(mapper, ReflectionTestUtils.getField(service, "mapper"));

			SqlSessionFactory factory = root.getBean(SqlSessionFactory.class);
			Configuration configuration = factory.getConfiguration();
			assertSame(source, configuration.getEnvironment().getDataSource());
			assertSame(source, root.getBean(DataSourceTransactionManager.class).getDataSource());
			assertTrue(configuration.hasMapper(AssigneeMapper.class));
			assertTrue(configuration.hasStatement(AssigneeMapper.class.getName() + ".getList"));
			assertTrue(configuration.hasStatement(AssigneeMapper.class.getName() + ".getTotal"));
			assertFalse(configuration.hasMapper(AssigneeService.class));
			assertFalse(configuration.hasMapper(ConnectionProbeMapper.class));
			assertFalse(configuration.hasStatement(ConnectionProbeMapper.class.getName() + ".selectProbe"));
			assertFalse(configuration.isResourceLoaded("hexa/erp/testinfra/ConnectionProbeMapper.xml"));
			assertTrue(root.getBeansOfType(ConnectionProbeMapper.class).isEmpty());

			for (String name : root.getBeanDefinitionNames()) {
				BeanDefinition definition = root.getBeanDefinition(name);
				String className = definition.getBeanClassName();
				assertFalse(name.endsWith("package-info"));
				if (className != null) {
					assertFalse(className.endsWith("package-info"));
					assertFalse(className.startsWith("hexa.erp.testinfra."));
					assertFalse(className.contains(".controller."));
				}
				if (MapperFactoryBean.class.getName().equals(className)) {
					Object mapperType = definition.getConstructorArgumentValues()
							.getGenericArgumentValues().get(0).getValue();
					String interfaceName = mapperType instanceof Class
							? ((Class<?>) mapperType).getName() : mapperType.toString();
					assertTrue(expectedPackages("mapper").contains(
							interfaceName.substring(0, interfaceName.lastIndexOf('.'))));
				}
			}
			for (Class<?> type : configuration.getMapperRegistry().getMappers()) {
				assertTrue(type.isInterface());
				assertFalse(type.getName().endsWith("package-info"));
				assertTrue(expectedPackages("mapper").contains(type.getPackage().getName()));
			}
		} finally {
			assertEquals("DB 연결을 한 번도 열면 안 된다.", 0, source.connectionAttempts);
		}
	}

	private Set<String> expectedPackages(String layer) {
		Set<String> packages = new LinkedHashSet<>();
		for (String business : BUSINESS_PACKAGES) {
			packages.add("hexa.erp." + business + "." + layer);
		}
		return packages;
	}

	private Set<String> declaredPackages(NodeList scans) {
		Set<String> packages = new LinkedHashSet<>();
		for (int i = 0; i < scans.getLength(); i++) {
			String attribute = ((Element) scans.item(i)).getAttribute("base-package");
			String[] names = StringUtils.tokenizeToStringArray(attribute,
					ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
			for (String name : names) {
				assertTrue("중복 스캔: " + name, packages.add(name));
			}
		}
		return packages;
	}

	private void loadRootDefinitions(GenericApplicationContext root) {
		new XmlBeanDefinitionReader(root).loadBeanDefinitions("file:" + ROOT_XML);
		assertNotNull(root.getBeanDefinition("sqlSessionFactory"));
	}

	/** 실수로 DB에 접근하면 즉시 실패한다. */
	private static class NoConnectionDataSource extends AbstractDataSource {
		private int connectionAttempts;

		@Override
		public Connection getConnection() throws SQLException {
			connectionAttempts++;
			throw new AssertionError("이 테스트는 실제 Connection을 사용하지 않는다.");
		}

		@Override
		public Connection getConnection(String username, String password) throws SQLException {
			return getConnection();
		}
	}
}
