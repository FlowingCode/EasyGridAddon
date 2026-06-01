package com.flowingcode.vaadin.addons.easygrid.it;
import com.flowingcode.vaadin.jsonmigration.InstrumentationViewInitializer;
import com.vaadin.flow.server.ServiceInitEvent;

@SuppressWarnings("serial")
public class ViewInitializerImpl extends InstrumentationViewInitializer {

  @Override
  public void serviceInit(ServiceInitEvent event) {
    registerInstrumentedRoute(EasyRowActionITView.class);
  }

}