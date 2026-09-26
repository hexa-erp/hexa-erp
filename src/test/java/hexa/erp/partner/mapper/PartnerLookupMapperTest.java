package hexa.erp.partner.mapper;

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

import hexa.erp.partner.domain.PartnerLookupVO;
import hexa.erp.common.domain.LookupCriteria;

/** Oracle 연결 없이 MyBatis XML·동적 SQL을 검사한다. SQL 실행 검사는 IT에서 한다. */
public class PartnerLookupMapperTest {
	private Configuration configuration;
	private static final String NAMESPACE = "hexa.erp.partner.mapper.PartnerLookupMapper";

	@Before
	public void loadMapperWithoutOpeningConnection() throws Exception {
		SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
		factory.setDataSource(new DriverManagerDataSource());
		factory.setConfigLocation(new ClassPathResource("mybatis-config.xml"));
		factory.setMapperLocations(
				new Resource[] { new ClassPathResource("hexa/erp/partner/mapper/PartnerLookupMapper.xml") });
		configuration = factory.getObject().getConfiguration();
	}

	@Test
	public void statementIdsAndResultTypeMatchMapperInterface() {
		assertTrue(configuration.hasMapper(PartnerLookupMapper.class));
		Set<String> methods = new HashSet<>();
		for (java.lang.reflect.Method method : PartnerLookupMapper.class.getDeclaredMethods()) {
			methods.add(method.getName());
		}
		for (String id : Arrays.asList("getListWithPaging", "getTotalCount", "read")) {
			assertTrue(methods.contains(id));
			assertEquals(SqlCommandType.SELECT, statement(id).getSqlCommandType());
		}
		assertEquals(PartnerLookupVO.class, statement("getListWithPaging").getResultMaps().get(0).getType());
		assertEquals(PartnerLookupVO.class, statement("read").getResultMaps().get(0).getType());
		assertEquals(Integer.class, statement("getTotalCount").getResultMaps().get(0).getType());
	}

	@Test
	public void camelCaseFieldsAndNullPolicyRemainConfigured() {
		assertTrue(configuration.isMapUnderscoreToCamelCase());
		assertEquals(JdbcType.NULL, configuration.getJdbcTypeForNull());
		MetaClass fields = MetaClass.forClass(PartnerLookupVO.class, new DefaultReflectorFactory());
		assertEquals("partnerId", fields.findProperty("PARTNER_ID", true));
		assertEquals("partnerCode", fields.findProperty("PARTNER_CODE", true));
		assertEquals("partnerName", fields.findProperty("PARTNER_NAME", true));
		assertEquals("assigneeId", fields.findProperty("ASSIGNEE_ID", true));
		assertEquals("assigneeCode", fields.findProperty("ASSIGNEE_CODE", true));
		assertEquals("assigneeName", fields.findProperty("ASSIGNEE_NAME", true));
		assertEquals(Long.class, fields.getGetterType("assigneeId"));
		assertEquals(String.class, fields.getGetterType("assigneeCode"));
		assertEquals(String.class, fields.getGetterType("assigneeName"));
		assertEquals("activeFlag", fields.findProperty("ACTIVE_FLAG", true));
		assertEquals(Long.class, fields.getGetterType("partnerId"));
		assertEquals(String.class, fields.getGetterType("partnerCode"));
		assertFalse(fields.hasGetter("rn"));
	}

	@Test
	public void emptyKeywordKeepsOnlyActiveConditionAndOracle11gBounds() {
		LookupCriteria criteria = new LookupCriteria();
		for (String keyword : Arrays.asList(null, "", "  ")) {
			criteria.setKeyword(keyword);
			BoundSql list = statement("getListWithPaging").getBoundSql(criteria);
			String sql = compact(list);
			assertTrue(sql.contains("FROM BIZ_PARTNER p LEFT JOIN ASSIGNEE a ON p.ASSIGNEE_ID = a.ASSIGNEE_ID "
					+ "WHERE p.ACTIVE_FLAG = 'Y' ORDER BY p.PARTNER_CODE ASC"));
			assertTrue(sql.contains("SELECT ROWNUM rn, ordered_partner.*"));
			assertTrue(sql.contains("WHERE ROWNUM <= ? * ?"));
			assertTrue(sql.contains("WHERE rn > (? - 1) * ?"));
			assertTrue(sql.endsWith("ORDER BY PARTNER_CODE ASC"));
			assertFalse(sql.contains("LIKE") || sql.contains("OFFSET") || sql.contains("FETCH"));
			assertEquals(Arrays.asList("pageNum", "amount", "pageNum", "amount"), parameters(list));
			BoundSql total = statement("getTotalCount").getBoundSql(criteria);
			assertEquals("SELECT COUNT(*) FROM BIZ_PARTNER p WHERE p.ACTIVE_FLAG = 'Y'", compact(total));
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
		String condition = "WHERE p.ACTIVE_FLAG = 'Y' AND ( UPPER(p.PARTNER_CODE) LIKE '%' || UPPER(?) || '%' "
				+ "OR UPPER(p.PARTNER_NAME) LIKE '%' || UPPER(?) || '%' )";
		assertTrue(compact(list).contains(condition));
		assertTrue(compact(total).endsWith(condition));
		assertFalse(compact(list).contains(criteria.getKeyword()));
		assertFalse(compact(total).contains(criteria.getKeyword()));
		assertEquals(Arrays.asList("keyword", "keyword", "pageNum", "amount", "pageNum", "amount"), parameters(list));
		assertEquals(Arrays.asList("keyword", "keyword"), parameters(total));
		assertFalse(compact(total).contains("ROWNUM") || compact(total).contains("ORDER BY"));
	}

	@Test
	public void assigneeColumnsUseLeftJoinWithoutFilteringPartnersOrChangingCount() {
		LookupCriteria criteria = new LookupCriteria();
		String sql = compact(statement("getListWithPaging").getBoundSql(criteria));
		assertTrue(sql.contains("a.ASSIGNEE_ID, a.ASSIGNEE_CODE, a.ASSIGNEE_NAME, p.ACTIVE_FLAG"));
		assertTrue(sql.contains("LEFT JOIN ASSIGNEE a ON p.ASSIGNEE_ID = a.ASSIGNEE_ID"));
		assertFalse(sql.contains("INNER JOIN") || sql.contains("a.ACTIVE_FLAG"));
		assertFalse(sql.contains("ASSIGNEE_ID IS NOT NULL"));
		assertFalse(compact(statement("getTotalCount").getBoundSql(criteria)).contains("JOIN"));
	}

	@Test
	public void selectedIdReadBindsTheIdAndIncludesInactiveRows() {
		BoundSql read = statement("read").getBoundSql(9007199254740993L);
		String sql = compact(read);
		assertTrue(sql.contains("p.PARTNER_ID, p.PARTNER_CODE, p.PARTNER_NAME"));
		assertTrue(sql.contains("FROM BIZ_PARTNER p"));
		assertTrue(sql.endsWith("WHERE p.PARTNER_ID = ?"));
		assertEquals(Arrays.asList("partnerId"), parameters(read));
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
}
