package com.relax.owl.common.log.domain.message;

import lombok.Data;
import lombok.ToString;
import com.relax.owl.common.log.constant.LogType;



/**
 * Operation data captured by the logging aspect.
 */
@Data
@ToString
public class OperationLogMessage {



    private LogType logType;
    private String module;
    private String action;
    private Long operatorId;
    private String method;
    private Boolean success;
    private Long cost;
    private String errorMsg;
    private String traceId;
}
