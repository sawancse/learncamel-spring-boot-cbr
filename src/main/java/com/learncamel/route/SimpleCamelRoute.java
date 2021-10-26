package com.learncamel.route;

import com.learncamel.domain.Item;
import lombok.extern.slf4j.Slf4j;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.apache.camel.spi.DataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

/**
 * Created by z001qgd on 1/24/18.
 */
@Component
@Slf4j
public class SimpleCamelRoute  extends RouteBuilder{


    @Autowired
    Environment environment;


    @Override
    public void configure() throws Exception {

        log.info("Starting the Camel Route");

        DataFormat bindy = new BindyCsvDataFormat(Item.class);

		
		from("{{startRoute}}")
		.log("Timer Invoked and the body" + environment.getProperty("message")).choice()
				.when((header("env").isNotEqualTo("mock")))
					.pollEnrich("{{fromRoute}}").otherwise()
					.log("mock env flow and the body is ${body}")
				.end()
				.to("{{toRoute1}}").unmarshal(bindy)
				.to("direct:a")
				.log("The unmarshaled object is ${body}").split(body()).log("Record is ${body}").end();
		 

        from("direct:a").process(new Processor(){

			@Override
			public void process(Exchange exchange) throws Exception {
				// TODO Auto-generated method stub
				log.info("body in a route: " +exchange.getIn().getBody());
				exchange.getIn().setHeader("foo", "foo");
				exchange.setProperty("foo", "foo");
				
			}
        	
        	})
        	.choice() 
        	.when(simple("${header.foo} == 'foo'"))
        		.to("direct:e")	
        		.to("direct:d") 
        	.when(simple("${property.foo} == 'foo'"))
        		.to("direct:e") 
        	.when(header("foo").isEqualTo("bar")) 
        		.to("direct:b") 
       		.when(header("foo").isEqualTo("cheese")) 
       			.to("direct:c") 
       		.otherwise() 
       		.to("direct:d"); 

        
        from("direct:d").process(new Processor(){

			@Override
			public void process(Exchange exchange) throws Exception {
				// TODO Auto-generated method stub
				log.info("body in a route d: " +exchange.getIn().getBody());
				
			}
        	
        	}).end();
        
        from("direct:e").process(new Processor(){

			@Override
			public void process(Exchange exchange) throws Exception {
				// TODO Auto-generated method stub
				log.info("body in a route e: " +exchange.getIn().getBody());
				
			}
        	
        	}).end();
        
        log.info("Ending the Camel Route");
        
        
    }
}
