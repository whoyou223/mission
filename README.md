# mission
一种轻量级的分布式定时任务解决方案，使用JAR方式引入即可。
1.使用方式
在正常使用spring的@Scheduled注解定义定时任务时，使用@Mission注解定义定时任务:
    @Scheduled(cron = "0 0 1 * * ?")
    @Mission(clazz = VoiceService.class, method = "update", retry = 1, parameter = true)
    public String mineMission01(){
        return "任务参数";
    }
并在spring启动时配置少量redis中key的名称，即可运行；
那么在cron指定的时间点将会执行注解中指定的类VoiceService的方法update，并以使用@Mission注解的方法的返回值作为update方法的参数。

2.功能
  a、分离任务生产和任务执行
  在分布式环境中无需指定唯一运行定时任务的节点，@Mission注解将拦截所有使用该注解的定时任务，将任务信息放入redis队列中，并由每个节点以指定
  时间间隔去消费队列中的任务。
  b、失败重试
  可指定@Mission的参数retry为任务失败后的重试次数，在任务执行失败后，放入失败队列，并由每个节点以指定时间间隔去单独重试。
  c、唯一性
  确保每个任务周期只生成一个任务，避免多节点重复执行任务；
  d、管理
  为每个任务生成全局唯一id，可通过接口实时查看任务生产、执行情况，可手动添加、移除任务黑名单，可查看任务执行日志；
  
