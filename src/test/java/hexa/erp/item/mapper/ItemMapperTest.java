package hexa.erp.item.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.BigDecimalTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import hexa.erp.item.domain.ItemVO;
import hexa.erp.common.domain.LookupCriteria;

/** Oracle 연결 없이 MyBatis XML·동적 SQL을 검사한다. SQL 실행 검사는 IT에서 한다. */
public class ItemMapperTest {
	private Configuration configuration;
	private static final String NAMESPACE = "hexa.erp.item.mapper.ItemMapper";

	@Before
	public void loadMapperWithoutOpeningConnection() throws Exception {
		SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
		factory.setDataSource(new DriverManagerDataSource());
		factory.setConfigLocation(new ClassPathResource("mybatis-config.xml"));
		factory.setMapperLocations(new Resource[] {
				new ClassPathResource("hexa/erp/item/mapper/ItemMapper.xml") });
		configuration = factory.getObject().getConfiguration();
	}

	@Test
	public void statementIdsAndResultTypeMatchMapperInterface() {
		assertTrue(configuration.hasMapper(ItemMapper.class));
		Set<String> statementNames = new HashSet<>();
		for (String name : configuration.getMappedStatementNames()) {
			if (name.startsWith(NAMESPACE + ".")) {
				statementNames.add(name.substring(NAMESPACE.length() + 1));
				assertEquals(SqlCommandType.SELECT, configuration.getMappedStatement(name).getSqlCommandType());
			}
		}
		assertEquals(new HashSet<>(Arrays.asList("getList", "getTotal")), statementNames);
		Set<String> methods = new HashSet<>();
		for (java.lang.reflect.Method method : ItemMapper.class.getDeclaredMethods()) {
			methods.add(method.getName());
		}
		assertEquals(methods, statementNames);
		assertEquals(ItemVO.class, statement("getList").getResultMaps().get(0).getType());
		assertEquals(Integer.class, statement("getTotal").getResultMaps().get(0).getType());
	}

	@Test
	public void camelCaseFieldsAndNullPolicyRemainConfigured() {
		assertTrue(configuration.isMapUnderscoreToCamelCase());
		assertEquals(JdbcType.NULL, configuration.getJdbcTypeForNull());
		MetaClass fields = MetaClass.forClass(ItemVO.class, new DefaultReflectorFactory());
		assertEquals("itemId", fields.findProperty("ITEM_ID", true));
		assertEquals("itemCode", fields.findProperty("ITEM_CODE", true));
		assertEquals("itemName", fields.findProperty("ITEM_NAME", true));
		assertEquals("activeFlag", fields.findProperty("ACTIVE_FLAG", true));
		assertEquals(Long.class, fields.getGetterType("itemId"));
		assertEquals(String.class, fields.getGetterType("itemCode"));
		assertEquals("specification", fields.findProperty("SPECIFICATION", true));
		assertEquals(String.class, fields.getGetterType("specification"));
		assertEquals("unit", fields.findProperty("UNIT", true));
		assertEquals(String.class, fields.getGetterType("unit"));
		assertEquals("outboundPrice", fields.findProperty("OUTBOUND_PRICE", true));
		assertEquals(BigDecimal.class, fields.getGetterType("outboundPrice"));
		assertEquals("stockQuantity", fields.findProperty("STOCK_QUANTITY", true));
		assertEquals(BigDecimal.class, fields.getGetterType("stockQuantity"));
		assertEquals(BigDecimalTypeHandler.class, configuration.getTypeHandlerRegistry()
				.getTypeHandler(BigDecimal.class, JdbcType.NUMERIC).getClass());
		assertFalse(fields.hasGetter("rn"));
		assertFalse(MetaClass.forClass(LookupCriteria.class, new DefaultReflectorFactory()).hasGetter("warehouseId"));
	}

	@Test
	public void emptyKeywordWithoutWarehouseKeepsItemsAndOracle11gBounds() throws Exception {
		LookupCriteria criteria = new LookupCriteria();
		for (String keyword : Arrays.asList(null, "", "  ")) {
			criteria.setKeyword(keyword);
			BoundSql list = listSql(criteria, null);
			String sql = compact(list);
			assertTrue(sql.contains("FROM ITEM i WHERE i.ACTIVE_FLAG = 'Y' ORDER BY i.ITEM_CODE ASC"));
			assertTrue(sql.contains("SELECT ROWNUM rn, ordered_item.*"));
			assertTrue(sql.contains("WHERE ROWNUM <= ? * ?"));
			assertTrue(sql.contains("WHERE rn > (? - 1) * ?"));
			assertTrue(sql.endsWith("ORDER BY ITEM_CODE ASC"));
			assertTrue(sql.contains("CAST(NULL AS NUMBER) AS STOCK_QUANTITY"));
			assertFalse(sql.contains("JOIN STOCK"));
			assertFalse(sql.contains("LIKE") || sql.contains("OFFSET") || sql.contains("FETCH"));
			assertEquals(Arrays.asList("criteria.pageNum", "criteria.amount", "criteria.pageNum", "criteria.amount"),
					parameters(list));
			BoundSql total = totalSql(criteria);
			assertEquals("SELECT COUNT(*) FROM ITEM i WHERE i.ACTIVE_FLAG = 'Y'", compact(total));
			assertTrue(total.getParameterMappings().isEmpty());
		}
	}

	@Test
	public void warehouseJoinKeepsItemsWithoutStockAndDoesNotFilterTotal() throws Exception {
		LookupCriteria criteria = new LookupCriteria();
		for (Long warehouseId : Arrays.asList(201L, 202L, 9007199254740993L)) {
			BoundSql list = listSql(criteria, warehouseId);
			String sql = compact(list);
			assertTrue(sql.contains("NVL(s.QUANTITY, 0) AS STOCK_QUANTITY"));
			assertTrue(sql.contains("FROM ITEM i LEFT JOIN STOCK s ON s.ITEM_ID = i.ITEM_ID"
					+ " AND s.WAREHOUSE_ID = ? WHERE i.ACTIVE_FLAG = 'Y'"));
			assertFalse(sql.contains("INNER JOIN") || sql.contains("STOCK_MOVEMENT"));
			assertFalse(sql.contains("WHERE s.") || sql.contains("AND s.QUANTITY"));
			assertEquals(Arrays.asList("warehouseId", "criteria.pageNum", "criteria.amount",
					"criteria.pageNum", "criteria.amount"), parameters(list));
			assertEquals("SELECT COUNT(*) FROM ITEM i WHERE i.ACTIVE_FLAG = 'Y'", compact(totalSql(criteria)));
		}
	}

	@Test
	public void listAndTotalBindTheSameKeywordInsteadOfInterpolatingSql() throws Exception {
		LookupCriteria criteria = new LookupCriteria();
		criteria.setPageNum(2);
		criteria.setKeyword("  00001' OR 1=1 -- %_  ");
		String condition = "WHERE i.ACTIVE_FLAG = 'Y' AND ( UPPER(i.ITEM_CODE) LIKE '%' || UPPER(?) || '%' "
				+ "OR UPPER(i.ITEM_NAME) LIKE '%' || UPPER(?) || '%' )";
		for (Long warehouseId : Arrays.asList(null, 201L)) {
			BoundSql list = listSql(criteria, warehouseId);
			BoundSql total = totalSql(criteria);
			assertTrue(compact(list).contains(condition));
			assertTrue(compact(total).endsWith(condition));
			assertFalse(compact(list).contains(criteria.getKeyword()));
			assertFalse(compact(total).contains(criteria.getKeyword()));
			List<String> expected = new ArrayList<>();
			if (warehouseId != null) expected.add("warehouseId");
			expected.addAll(Arrays.asList("criteria.keyword", "criteria.keyword", "criteria.pageNum",
					"criteria.amount", "criteria.pageNum", "criteria.amount"));
			assertEquals(expected, parameters(list));
			assertEquals(Arrays.asList("criteria.keyword", "criteria.keyword"), parameters(total));
			assertFalse(compact(total).contains("STOCK") || compact(total).contains("ROWNUM")
					|| compact(total).contains("ORDER BY"));
		}
	}

	private BoundSql listSql(LookupCriteria criteria, Long warehouseId) throws Exception {
		Object values = new ParamNameResolver(configuration,
				ItemMapper.class.getMethod("getList", LookupCriteria.class, Long.class))
				.getNamedParams(new Object[] { criteria, warehouseId });
		assertSame(criteria, ((Map<?, ?>) values).get("criteria"));
		assertEquals(warehouseId, ((Map<?, ?>) values).get("warehouseId"));
		return statement("getList").getBoundSql(values);
	}

	private BoundSql totalSql(LookupCriteria criteria) throws Exception {
		Object values = new ParamNameResolver(configuration,
				ItemMapper.class.getMethod("getTotal", LookupCriteria.class))
				.getNamedParams(new Object[] { criteria });
		assertSame(criteria, ((Map<?, ?>) values).get("criteria"));
		return statement("getTotal").getBoundSql(values);
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
