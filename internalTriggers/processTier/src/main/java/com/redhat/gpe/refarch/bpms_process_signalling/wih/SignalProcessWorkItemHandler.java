package com.redhat.gpe.refarch.bpms_process_signalling.wih;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;

public class SignalProcessWorkItemHandler implements WorkItemHandler {

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        
        
        String deploymentId = ((WorkItemImpl) workItem).getDeploymentId();
        RuntimeManager rm = RuntimeManagerRegistry.get().getManager(deploymentId);
        
        String signalType = (String)workItem.getParameter("signalType");
        Object event = workItem.getParameter("event");
        if (event == null) {
            event = Long.toString(workItem.getProcessInstanceId());
        }
        
        String processToSignal = (String)workItem.getParameter("processToSignal");
        RuntimeEngine engine = null;
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
        
        manager.completeWorkItem(workItem.getId(), null);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        
    }

}
