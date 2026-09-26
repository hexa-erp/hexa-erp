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

import hexa.erp.assignee.mapper.AssigneeLookupMapper;
import hexa.erp.assignee.service.AssigneeLookupService;
import hexa.erp.assignee.service.AssigneeLookupServiceImpl;
import hexa.erp.partner.mapper.PartnerLookupMapper;
import hexa.erp.partner.service.PartnerLookupService;
import hexa.erp.partner.service.PartnerLookupServiceImpl;
import hexa.erp.warehouse.mapper.WarehouseLookupMapper;
import hexa.erp.warehouse.service.WarehouseLookupService;
import hexa.erp.warehouse.service.WarehouseLookupServiceImpl;
import hexa.erp.item.mapper.ItemLookupMapper;
import hexa.erp.item.service.ItemLookupService;
import hexa.erp.item.service.ItemLookupServiceImpl;

/** Oracle 없이 업무별 스캔·XML 로딩·빈 주입을 검사한다. 테스트 DataSource는 연결을 금지한다. */
public class RootContextConfigurationTest {
	private static final String ROOT_XML = "src/main/webapp/WEB-INF/spring/root-context.xml";
	private static final String MAPPER_PATTERN = "classpath*:hexa/erp/*/mapper/*Mapper.xml";
	private static final String[] BUSINESS_PACKAGES = { "assignee", "partner", "warehouse", "item", "quotation",
			"salesorder", "sale", "shipinstruction", "shipment", "stock", "common" };

	@Test
	public void scanDeclarationsIncludeRequiredMapperAndServicePackages() throws Exception {
		DocumentBuilderFactory builder = DocumentBuilderFactory.newInstance();
		builder.setNamespaceAware(true);
		builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		Document xml = builder.newDocumentBuilder().parse(new File(ROOT_XML));

		NodeList mapperScans = xml.getElementsByTagNameNS("http://mybatis.org/schema/mybatis-spring", "scan");
		NodeList serviceScans = xml.getElementsByTagNameNS("http://www.springframework.org/schema/context",
				"component-scan");
		assertTrue(declaredPackages(mapperScans).containsAll(expectedPackages("mapper")));
		assertTrue(declaredPackages(serviceScans).containsAll(expectedPackages("service")));
		for (int i = 0; i < mapperScans.getLength(); i++) {
			Element scan = (Element) mapperScans.item(i);
			assertEquals("sqlSessionFactory", scan.getAttribute("factory-ref"));
			assertFalse(scan.hasAttribute("annotation"));
			assertFalse(scan.hasAttribute("marker-interface"));
		}
	}

	@Test
	public void wildcardFindsBasicLookupXmlsButExcludesVisibleConnectionProbeXml() throws Exception {
		try (GenericApplicationContext root = new GenericApplicationContext()) {
			loadRootDefinitions(root);
			Object configured = root.getBeanDefinition("sqlSessionFactory").getPropertyValues().get("mapperLocations");
			String pattern = configured instanceof TypedStringValue ? ((TypedStringValue) configured).getValue()
					: configured.toString();
			assertEquals(MAPPER_PATTERN, pattern);

			Set<String> locations = new HashSet<>();
			for (Resource resource : new PathMatchingResourcePatternResolver().getResources(pattern)) {
				assertTrue(resource.exists());
				assertFalse(resource.getURL().toExternalForm().contains("/testinfra/"));
				locations.add(resource.getURL().toExternalForm());
			}
			assertTrue(locations.contains(new ClassPathResource("hexa/erp/assignee/mapper/AssigneeLookupMapper.xml")
					.getURL().toExternalForm()));

			assertTrue(locations.contains(new ClassPathResource("hexa/erp/partner/mapper/PartnerLookupMapper.xml")
					.getURL().toExternalForm()));
			assertTrue(locations.contains(new ClassPathResource("hexa/erp/warehouse/mapper/WarehouseLookupMapper.xml")
					.getURL().toExternalForm()));
			assertTrue(locations.contains(
					new ClassPathResource("hexa/erp/item/mapper/ItemLookupMapper.xml").getURL().toExternalForm()));

			// 테스트 XML이 있어도 업무 Mapper 검색에는 포함되면 안 된다.
			Resource probe = new ClassPathResource("hexa/erp/testinfra/ConnectionProbeMapper.xml");
			assertTrue(probe.exists());
			assertFalse(locations.contains(probe.getURL().toExternalForm()));
			assertFalse(root.isActive());
			assertFalse(root.getBeanFactory().containsSingleton("dataSource"));
		}
	}

	@Test
	public void actualRootRegistersBasicLookupGraphsWithoutOpeningAnyConnection() {
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
			AssigneeLookupMapper mapper = root.getBean(AssigneeLookupMapper.class);
			AssigneeLookupService service = root.getBean(AssigneeLookupService.class);
			assertEquals(1, root.getBeansOfType(AssigneeLookupMapper.class).size());
			assertEquals(1, root.getBeansOfType(AssigneeLookupService.class).size());
			assertTrue(service instanceof AssigneeLookupServiceImpl);
			assertTrue(AssigneeLookupServiceImpl.class.isAnnotationPresent(Service.class));
			assertFalse(AssigneeLookupService.class.isAnnotationPresent(Service.class));
			assertSame(mapper, ReflectionTestUtils.getField(service, "mapper"));

			SqlSessionFactory factory = root.getBean(SqlSessionFactory.class);
			Configuration configuration = factory.getConfiguration();
			assertSame(source, configuration.getEnvironment().getDataSource());
			assertSame(source, root.getBean(DataSourceTransactionManager.class).getDataSource());
			assertTrue(configuration.hasMapper(AssigneeLookupMapper.class));
			assertTrue(configuration.hasStatement(AssigneeLookupMapper.class.getName() + ".getListWithPaging"));
			assertTrue(configuration.hasStatement(AssigneeLookupMapper.class.getName() + ".getTotalCount"));
			assertFalse(configuration.hasMapper(AssigneeLookupService.class));
			assertLookupGraph(root, configuration, PartnerLookupMapper.class, PartnerLookupService.class,
					PartnerLookupServiceImpl.class);
			assertLookupGraph(root, configuration, WarehouseLookupMapper.class, WarehouseLookupService.class,
					WarehouseLookupServiceImpl.class);
			assertLookupGraph(root, configuration, ItemLookupMapper.class, ItemLookupService.class,
					ItemLookupServiceImpl.class);
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
					Object mapperType = definition.getConstructorArgumentValues().getGenericArgumentValues().get(0)
							.getValue();
					String interfaceName = mapperType instanceof Class ? ((Class<?>) mapperType).getName()
							: mapperType.toString();
					assertTrue(interfaceName.startsWith("hexa.erp."));
					assertTrue(interfaceName.substring(0, interfaceName.lastIndexOf('.')).endsWith(".mapper"));
				}
			}
			for (Class<?> type : configuration.getMapperRegistry().getMappers()) {
				assertTrue(type.isInterface());
				assertFalse(type.getName().endsWith("package-info"));
				assertTrue(type.getPackage().getName().startsWith("hexa.erp."));
				assertTrue(type.getPackage().getName().endsWith(".mapper"));
			}
		} finally {
			assertEquals("DB 연결을 한 번도 열면 안 된다.", 0, source.connectionAttempts);
		}
	}

	private void assertLookupGraph(GenericApplicationContext root, Configuration configuration, Class<?> mapperType,
			Class<?> serviceType, Class<?> implementationType) {
		assertEquals(1, root.getBeansOfType(mapperType).size());
		assertEquals(1, root.getBeansOfType(serviceType).size());
		Object service = root.getBean(serviceType);
		assertTrue(implementationType.isInstance(service));
		assertTrue(implementationType.isAnnotationPresent(Service.class));
		assertFalse(serviceType.isAnnotationPresent(Service.class));
		assertSame(root.getBean(mapperType), ReflectionTestUtils.getField(service, "mapper"));
		assertTrue(configuration.hasMapper(mapperType));
		assertTrue(configuration.hasStatement(mapperType.getName() + ".getListWithPaging"));
		assertTrue(configuration.hasStatement(mapperType.getName() + ".getTotalCount"));
		assertFalse(configuration.hasMapper(serviceType));
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
