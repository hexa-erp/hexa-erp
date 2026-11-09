package hexa.erp.testinfra;

import org.apache.ibatis.annotations.Param;

/** DUALで接続を確認するテスト専用Mapper。database-test-context.xmlでのみ登録する。 */
public interface ConnectionProbeMapper {
	ConnectionProbeRow selectProbe(@Param("probeValue") String probeValue, @Param("optionalNote") String optionalNote);
}
