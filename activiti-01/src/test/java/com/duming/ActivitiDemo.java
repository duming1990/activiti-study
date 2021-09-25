package com.duming;

import org.activiti.engine.*;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipInputStream;

public class ActivitiDemo {
    /*
        测试流程部署
     */
    @Test
    public void testDeployment(){
        //1.创建ProcessEngine
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

        //2.获取RepositoryService
        RepositoryService repositoryService = processEngine.getRepositoryService();

        //3.使用service进行流程部署，定义一个流程的名字，把bpmn和png部署到数据中
        Deployment deploy = repositoryService.createDeployment()
                .name("出差申请")
                .addClasspathResource("bpmn/evection.bpmn")
                .addClasspathResource("bpmn/evection.png")
                .deploy();

        System.out.println("流程部署id->"+deploy.getId());
        System.out.println("流程部署名称->"+deploy.getName());

    }

    /*
        启动流程实例
        act_hi_actinst  流程实例执行历史
        act_hi_identitylink 流程参与者的历史信息
        act_hi_procinst 流程实例历史信息
        act_hi_taskinst 任务历史信息
        act_ru_execution    流程执行的信息
        act_ru_identitylink 流程参与者信息
        act_ru_task 任务信息
     */
    @Test
    public void testStartProcess(){
        //1.创建ProcessEngine
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        //2.获取RuntimeService
        RuntimeService runtimeService = processEngine.getRuntimeService();
        //3.根据流程定义的id启动流程
        ProcessInstance instance = runtimeService.startProcessInstanceByKey("myEvection");
        System.out.println("流程定义ID->"+instance.getProcessDefinitionId());
        System.out.println("流程实例ID->"+instance.getId());
        System.out.println("当前活动ID->"+instance.getActivityId());
    }

    /*
     *  查询个人待执行的任务
     */
    @Test
    public void testFindPersonTaskList(){
        //1.创建ProcessEngine
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        //2.获取TaskService
        TaskService taskService = processEngine.getTaskService();
        //3.根据流程 key和 任务负责人 查询任务
        List<Task> list = taskService.createTaskQuery()
                .processDefinitionKey("myEvection") //流程key
                .taskAssignee("zhangsan")   //要查询的负责人
                .list();

        for (Task task : list) {
            System.out.println("流程定义的id->"+task.getProcessDefinitionId());
            System.out.println("流程实例的id->"+task.getProcessInstanceId());
            System.out.println("任务的id->"+task.getId());
            System.out.println("任务负责人->"+task.getAssignee());
            System.out.println("任务名称->"+task.getName()  );
        }
    }

    /*
     *  完成个人任务
     */
    @Test
    public void conpletTask(){
        //1.获取流程引擎ProcessEngine
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        //2.获取TaskService
        TaskService taskService = processEngine.getTaskService();
        //3.根据任务id完成任务
//        taskService.complete("5005");//完成zhangsan的任务

        //获取 jerry-myEvection 对应的任务
        Task task = taskService.createTaskQuery()
                .processDefinitionKey("myEvection")
                .taskAssignee("rose")
                .singleResult();
        //完成jerry的任务
        taskService.complete(task.getId());
    }

    /**
     *  使用zip包来进行批量的部署
     */
    @Test
    public void deployProcessByZip(){
        //1.获取ProcessEngine
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        //2.获取RepositoryService
        RepositoryService repositoryService = processEngine.getRepositoryService();
        //3.流程部署
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("bpmn/evection.zip");

        ZipInputStream zipInputStream = new ZipInputStream(inputStream);

        Deployment deploy = repositoryService.createDeployment()
                .addZipInputStream(zipInputStream)
                .deploy();

        System.out.println("流程部署id："+deploy.getId());
        System.out.println("流程部署名称："+deploy.getName());

    }

    /**
     *  查询流程定义
     */
    @Test
    public void queryProcessDefinition(){
        //1.获取流程引擎
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        //2.获取RepositoryService
        RepositoryService repositoryService = processEngine.getRepositoryService();
        //3.获取ProcessDefinitionQuery对象
        ProcessDefinitionQuery definitionQuery = repositoryService.createProcessDefinitionQuery();
        //4.查询当前的所有流程定义,返回流程定义信息的集合
        List<ProcessDefinition> myEvection = definitionQuery.processDefinitionKey("myEvection")//processDefinitionKey(流程定义key)
                .orderByProcessDefinitionVersion() //根据version排序
                .desc()     //倒叙
                .list();    //查出所有内容

        for (ProcessDefinition processDefinition : myEvection) {
            System.out.println("流程定义id："+processDefinition.getId());
            System.out.println("流程定义名称："+processDefinition.getName());
            System.out.println("流程定义key："+processDefinition.getKey());
            System.out.println("流程定义版本："+processDefinition.getVersion());
            System.out.println("流程部署id："+processDefinition.getDeploymentId());
        }
    }

    /**
     *  删除流程部署信息
     *  act_ge_bytearray
     *  act_re_deployment
     *  act_re_procdef
     *  如果流程已启用，但没有完成，想要删除的话需要使用特殊方式    级联删除
     */
    @Test
    public void deleteDeployMent(){
        //1.获取流程引擎
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        //2.获取RepositoryService
        RepositoryService repositoryService = processEngine.getRepositoryService();
        //3.通过部署id删除部署信息
        String deploymentid="1";
        repositoryService.deleteDeployment(deploymentid);
        repositoryService.deleteDeployment(deploymentid, true);//级联删除
    }

    /**
     *  下载 资源文件
     *  方案1：使用Activiti提供的api
     *  方案2：自己写代码从数据库中下载，使用jdbc 对blob类型，clob类型数据读取出来，保存到文件目录
     *  解决io操作：commons-io.jar
     *  这里使用方案1，RepositoryService
     */
    @Test
    public void getDeployMent(){

    }
}
