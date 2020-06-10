/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ceos.merlot.testplc4xosgiway.command;

import java.util.ServiceLoader;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.PlcDriver;
import org.apache.plc4x.java.api.messages.PlcReadRequest;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.apache.plc4x.java.spi.transport.Transport;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 *
 * @author cgarcia
 */
@Command(scope = "plc4x", name = "read", description = "Read a list of fields from PLC.")
@Service
public class MyCommand implements Action {

    @Reference
    BundleContext bc;
    
    @Reference
    Transport trans;
    
    @Argument(index = 0, name = "code", description = "PLC4X Device.", required = true, multiValued = false)
    String code;  

    @Argument(index = 1, name = "url", description = "PLC4X URL Conection.", required = true, multiValued = false)
    String url;   
    
    @Argument(index = 2, name = "tag", description = "Tag PLC4X to read.", required = true, multiValued = false)
    String tag;     
    
    
    
    @Override
    public Object execute() throws Exception {
        System.out.println("Name: " + trans.getTransportName());
        System.out.println("Code: " +trans.getTransportCode());
        

        /*
        
        Optional<Transport> optTransport = ldr.findFirst();
        if (optTransport.isPresent()){
            System.out.println("Name: " + optTransport.get().getTransportName());
            System.out.println("Code: " + optTransport.get().getTransportCode());
        } else {
            System.out.println("Transport not found...");
        }
        */
        
        String filter = "(&(" + org.osgi.framework.Constants.OBJECTCLASS + "=org.apache.plc4x.java.api.PlcDriver)" +
                        "(org.apache.plc4x.driver.code=" + code + "))";  
        
        ServiceReference[] refs = bc.getServiceReferences((String) null, filter);
        if (refs == null){
            System.out.println("Device not found. SERIAL: " + code);
        } else {
            ServiceReference ref = refs[0];
            System.out.println("Find driver service: " + ref.getProperty("org.apache.plc4x.driver.name"));
            PlcDriver theDriver = (PlcDriver) bc.getService(ref);
            
            //ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            //Thread.currentThread().setContextClassLoader(bc.getBundle().adapt(BundleWiring.class).getClassLoader());

            ServiceLoader<Transport> transports = ServiceLoader.load(Transport.class);
            for (Transport transport : transports) {
                System.out.println("Name..:" + transport.getTransportName());
                System.out.println("Code..:" + transport.getTransportCode());
            }            
            
            PlcConnection plcConnection = theDriver.getConnection(url);
            
            //Thread.currentThread().setContextClassLoader(tccl);
            
            PlcReadRequest.Builder builder = plcConnection.readRequestBuilder();
            builder.addItem("handler",tag); 
            PlcReadRequest readRequest = builder.build();
            
            System.out.println("Synchronous request ...");
            long start = System.currentTimeMillis();
            int i = 0;
            plcConnection.connect();
            for (i=0; i < 1000; i++) {
                PlcReadResponse syncResponse = readRequest.execute().get();            
                PlcResponseCode asPlcValue = syncResponse.getResponseCode("handler");
                if (asPlcValue != PlcResponseCode.OK) System.out.println("Error..." + i);
            }    
            
            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            
            System.out.println("Caso 1: Tiempo en ms: " + timeElapsed);            
            System.out.println("Close connection...");
            Thread.sleep(1000);
            plcConnection.close();              
        }             

        return null;
    }
    
}
