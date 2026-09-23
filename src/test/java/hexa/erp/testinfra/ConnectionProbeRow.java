package hexa.erp.testinfra;

import lombok.Data;

/** 업무 데이터가 아닌 연결 점검용 결과 객체. camelCase·null 매핑을 확인한다. */
@Data
public class ConnectionProbeRow {
	private String probeValue;
	private String optionalNote;
}
