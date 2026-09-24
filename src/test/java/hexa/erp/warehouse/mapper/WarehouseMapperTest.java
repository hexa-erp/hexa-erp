package hexa.erp.warehouse.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import hexa.erp.warehouse.domain.WarehouseVO;
import hexa.erp.common.domain.LookupCriteria;

/** Oracle 연결 없이 MyBatis XML·동적 SQL을 검사한다. SQL 실행 검사는 IT에서 한다. */
public class WarehouseMapperTest {
	private Configuration configuration;
	private static final String NAMESPACE = "hexa.erp.warehouse.mapper.WarehouseMapper";

	@Before
	public void loadMapperWithoutOpeningConnection() throws Exception {
		SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
		factory.setDataSource(new DriverManagerDataSource());
		factory.setConfigLocation(new ClassPathResource("mybatis-config.xml"));
		factory.setMapperLocations(new Resource[] {
				new ClassPathResource("hexa/erp/warehouse/mapper/WarehouseMapper.xml") });
		configuration = factory.getObject().getConfiguration();
	}

	@Test
	public void statementIdsAndResultTypeMatchMapperInterface() {
		assertTrue(configuration.hasMapper(WarehouseMapper.class));
		Set<String> statementNames = new HashSet<>();
		for (String name : configuration.getMappedStatementNames()) {
			if (name.startsWith(NAMESPACE + ".")) {
				statementNames.add(name.substring(NAMESPACE.length() + 1));
				assertEquals(SqlCommandType.SELECT, configuration.getMappedStatement(name).getSqlCommandType());
			}
		}
		assertEquals(new HashSet<>(Arrays.asList("getList", "getTotal")), statementNames);
		Set<String> methods = new HashSet<>();
		for (java.lang.reflect.Method method : WarehouseMapper.class.getDeclaredMethods()) {
			methods.add(method.getName());
		}
		assertEquals(methods, statementNames);
		assertEquals(WarehouseVO.class, statement("getList").getResultMaps().get(0).getType());
		assertEquals(Integer.class, statement("getTotal").getResultMaps().get(0).getType());
	}

	@Test
	public void camelCaseFieldsAndNullPolicyRemainConfigured() {
		assertTrue(configuration.isMapUnderscoreToCamelCase());
		assertEquals(JdbcType.NULL, configuration.getJdbcTypeForNull());
		MetaClass fields = MetaClass.forClass(WarehouseVO.class, new DefaultReflectorFactory());
		assertEquals("warehouseId", fields.findProperty("WAREHOUSE_ID", true));
		assertEquals("warehouseCode", fields.findProperty("WAREHOUSE_CODE", true));
		assertEquals("warehouseName", fields.findProperty("WAREHOUSE_NAME", true));
		assertEquals("activeFlag", fields.findProperty("ACTIVE_FLAG", true));
		assertEquals(Long.class, fields.getGetterType("warehouseId"));
		assertEquals(String.class, fields.getGetterType("warehouseCode"));
		assertEquals("warehouseType", fields.findProperty("WAREHOUSE_TYPE", true));
		assertEquals(String.class, fields.getGetterType("warehouseType"));
		assertFalse(fields.hasGetter("rn"));
	}

	@Test
	public void emptyKeywordKeepsOnlyActiveConditionAndOracle11gBounds() {
		LookupCriteria criteria = new LookupCriteria();
		for (String keyword : Arrays.asList(null, "", "  ")) {
			criteria.setKeyword(keyword);
			BoundSql list = statement("getList").getBoundSql(criteria);
			String sql = compact(list);
			assertTrue(sql.contains("FROM WAREHOUSE WHERE ACTIVE_FLAG = 'Y' ORDER BY WAREHOUSE_CODE ASC"));
			assertTrue(sql.contains("SELECT ROWNUM rn, ordered_warehouse.*"));
			assertTrue(sql.contains("WHERE ROWNUM <= ? * ?"));
			assertTrue(sql.contains("WHERE rn > (? - 1) * ?"));
			assertTrue(sql.endsWith("ORDER BY WAREHOUSE_CODE ASC"));
			assertFalse(sql.contains("LIKE") || sql.contains("OFFSET") || sql.contains("FETCH"));
			assertEquals(Arrays.asList("pageNum", "amount", "pageNum", "amount"), parameters(list));
			BoundSql total = statement("getTotal").getBoundSql(criteria);
			assertEquals("SELECT COUNT(*) FROM WAREHOUSE WHERE ACTIVE_FLAG = 'Y'", compact(total));
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
		String condition = "WHERE ACTIVE_FLAG = 'Y' AND ( UPPER(WAREHOUSE_CODE) LIKE '%' || UPPER(?) || '%' "
				+ "OR UPPER(WAREHOUSE_NAME) LIKE '%' || UPPER(?) || '%' )";
		assertTrue(compact(list).contains(condition));
		assertTrue(compact(total).endsWith(condition));
		assertFalse(compact(list).contains(criteria.getKeyword()));
		assertFalse(compact(total).contains(criteria.getKeyword()));
		assertEquals(Arrays.asList("keyword", "keyword", "pageNum", "amount", "pageNum", "amount"), parameters(list));
		assertEquals(Arrays.asList("keyword", "keyword"), parameters(total));
		assertFalse(compact(total).contains("ROWNUM") || compact(total).contains("ORDER BY"));
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
}
