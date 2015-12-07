package com.redhat.gpe.refarch.bpms_process_signalling.wih;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkItem;
import org.kie.internal.executor.api.Command;
import org.kie.internal.executor.api.CommandContext;
import org.kie.internal.executor.api.ExecutionResults;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;

public class SignalProcessCommand implements Command {

    @Override
    public ExecutionResults execute(CommandContext ctx) throws Exception {
        RuntimeManager rm = null;
        RuntimeEngine engine = null;
        try {
            WorkItem workItem = (WorkItem) ctx.getData("workItem");
            String deploymentId = ((WorkItemImpl) workItem).getDeploymentId();
            rm = RuntimeManagerRegistry.get().getManager(deploymentId);
            
            String signalType = (String)workItem.getParameter("signalType");
            Object event = workItem.getParameter("event");
            if (event == null) {
                event = Long.toString(workItem.getProcessInstanceId());
            }
            
            String processToSignal = (String)workItem.getParameter("processToSignal");
            long processToSignalId = 0;
            if (processToSignal == null || processToSignal.isEmpty()) {
                engine = rm.getRuntimeEngine(EmptyContext.get());
            } else {
                processToSignalId = Long.valueOf(processToSignal);
                engine = rm.getRuntimeEngine(ProcessInstanceIdContext.get(processToSignalId));
            }

            KieSession ksession = engine.getKieSession();
            if (processToSignalId <= 0) {
                ksession.signalEvent(signalType, event); 
            } else {
                ksession.signalEvent(signalType, event, processToSignalId);
            }        
            return new ExecutionResults();
        } finally {
            if (rm != null) {
                rm.disposeRuntimeEngine(engine);;
            }
        }
    }

}
