package hexa.erp.testinfra;

import org.apache.ibatis.annotations.Param;

/** DUAL로 연결을 점검하는 테스트 전용 Mapper. database-test-context.xml에서만 등록한다. */
public interface ConnectionProbeMapper {
	ConnectionProbeRow selectProbe(@Param("probeValue") String probeValue,
			@Param("optionalNote") String optionalNote);
}
