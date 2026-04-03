package com.flowingcode.vaadin.addons.easygrid;

import com.flowingcode.vaadin.addons.demo.DemoSource;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@DemoSource
@PageTitle("Easy Grid Add-on Demo")
@SuppressWarnings("serial")
@Route(value = "demo", layout = EasyGridDemoView.class)
public class EasyGridDemo extends Div {

  public EasyGridDemo() {
    add(new EasyGridAddon());
  }
}
