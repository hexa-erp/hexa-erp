package hexa.erp.testinfra;

import lombok.Data;

/** 業務データではなく接続確認用の結果オブジェクト。camelCase・nullマッピングを確認する。 */
@Data
public class ConnectionProbeRow {
	private String probeValue;
	private String optionalNote;
}
