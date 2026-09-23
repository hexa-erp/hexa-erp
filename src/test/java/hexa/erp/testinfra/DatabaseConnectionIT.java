package hexa.erp.testinfra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.inject.Inject;
import javax.sql.DataSource;
import lombok.extern.log4j.Log4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 실제 Oracle 연결 점검: mvn -Dtest=DatabaseConnectionIT test
 * 기본 테스트에서 제외하며 DUAL 조회만 수행한다.
 */
@Log4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:src/main/webapp/WEB-INF/spring/root-context.xml",
		"classpath:spring/database-test-context.xml" })
public class DatabaseConnectionIT {
	@Inject
	private DataSource dataSource;

	@Inject
	private SqlSessionFactory sqlSessionFactory;

	@Inject
	private DataSourceTransactionManager transactionManager;

	@Inject
	private ConnectionProbeMapper connectionProbeMapper;

	@Test
	public void oracleConnectionAndTransactionManagerUseConfiguredDataSource() throws Exception {
		assertSame(dataSource, sqlSessionFactory.getConfiguration().getEnvironment().getDataSource());
		assertSame(dataSource, transactionManager.getDataSource());
		try (Connection connection = dataSource.getConnection();
				Statement statement = connection.createStatement();
				ResultSet rows = statement.executeQuery("SELECT 1 FROM DUAL")) {
			assertTrue(rows.next());
			assertEquals(1, rows.getInt(1));
		}
		log.info("Oracle 연결 및 공통 DataSource 설정 확인 완료");
	}

	@Test
	public void mapperXmlSupportsNullParametersAndCamelCaseResults() {
		ConnectionProbeRow row = connectionProbeMapper.selectProbe("team-dev", null);
		assertEquals("team-dev", row.getProbeValue());
		assertNull(row.getOptionalNote());
	}

	@Test
	public void mapperParticipatesInReadOnlySpringTransaction() {
		TransactionTemplate transaction = new TransactionTemplate(transactionManager);
		transaction.setReadOnly(true);
		ConnectionProbeRow row = transaction.execute(status -> {
			assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
			assertTrue(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
			assertTrue(TransactionSynchronizationManager.hasResource(dataSource));
			ConnectionProbeRow selected = connectionProbeMapper.selectProbe("transaction", null);
			assertTrue(TransactionSynchronizationManager.hasResource(sqlSessionFactory));
			return selected;
		});
		assertEquals("transaction", row.getProbeValue());
		assertNull(row.getOptionalNote());
	}
}
