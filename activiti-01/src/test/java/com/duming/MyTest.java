package com.duming;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

public class MyTest {

    @Test
    public void creatTable(){
        //ProcessEngines.getDefaultProcessEngine会加载activiti.cfg.xml文件
        //创建processEngine时会创建mysql表
        //默认方式
//        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
//        System.out.println(processEngine);
//        RepositoryService repositoryService = processEngine.getRepositoryService();
//        repositoryService.createDeployment();

        //使用自定义的方式
        //配置文件的名字可以自定义,bean的名字也可以自定义
        ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration
                .createProcessEngineConfigurationFromResource("activiti.cfg.xml","processEngineConfiguration");

        //获取流程引擎对象
        ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();


    }
}
