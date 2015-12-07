package com.redhat.gpe.refarch.bpm_signalling.wih;

import java.util.Map;
import java.util.Map.Entry;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.process.workitem.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
    BPMN2 Message Events require a custom WorkItemHandler that is mapped to a Service Task name of:  Send Task.
    There is a workItemHandler that comes with BPM Suite 6 called:  org.jbpm.bpmn2.handler.SendTaskHandler .
        - However, this class is a no-op implementation.

    This implementation demonstrates the sending of a message event.
*/
public class MessageEventSendHandler extends AbstractLogOrThrowWorkItemHandler {

    // As per various classes in 'jbpm-bpmn2' source, a message event must start with the following prefix   
    private static final String MESSAGE_PREFIX = "Message-"; 

    // Seems that jbpm automatically assigns task variable a name of "Message"
    private static final String MESSAGE_PAYLOAD = "Message";

    private static final String MESSAGE_NAME = "taskMessageName"; 

    private static final Logger log = LoggerFactory.getLogger(MessageEventSendHandler.class);

    private KieSession sessionObj = null;

    public MessageEventSendHandler() {}

    public MessageEventSendHandler(KieSession sessionObj) {
    this.sessionObj = sessionObj;
        this.setLogThrownException(true);
    }

    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        try {
            RuleFlowProcessInstance pInstance = (RuleFlowProcessInstance)sessionObj.getProcessInstance(workItem.getProcessInstanceId());
            String taskMessageName = (String)workItem.getParameter(MESSAGE_NAME);
            //if(taskMessageName == null || taskMessageName.equals(""))
            //    throw new Exception("executeWorkItem() must pass a task variable of: "+MESSAGE_NAME);
            final String taskMessagePayload = (String)workItem.getParameter(MESSAGE_PAYLOAD);
            if(taskMessagePayload == null || taskMessagePayload.equals(""))
                throw new Exception("executeWorkItem() must pass a task variable of: "+MESSAGE_PAYLOAD);

            

            dumpParameters(workItem);
            log.info("executeWorkItem() taskMessagePayload = "+taskMessagePayload);

            //new Thread(new Runnable() {
            //    public void run() {
                    sessionObj.signalEvent(MESSAGE_PREFIX+"A", taskMessagePayload);
            //    }
            //}).start();
            manager.completeWorkItem(workItem.getId(), null);
        }catch(Throwable x) {
            x.printStackTrace();
            handleException(x);
        }
    }

    public void dumpParameters(WorkItem workItem) {
        Map<String, Object> parameters = workItem.getParameters();
        StringBuilder sBuilder = new StringBuilder();
        for(Entry<String, Object> param : parameters.entrySet()) {
            sBuilder.append("\n\t"+param.getKey()+" : "+param.getValue());
        }
        log.info("dumpParameters()"+sBuilder.toString());
    }

    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        log.warn("abortWorkItem() what should be the biz-logic here ?");
    }

}
