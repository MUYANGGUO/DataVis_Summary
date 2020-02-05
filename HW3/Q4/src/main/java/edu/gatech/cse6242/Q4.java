package edu.gatech.cse6242;

import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Q4 {
    
  public static class FirstMapper extends Mapper<Object,Text,IntWritable,IntWritable>{
	private IntWritable src = new IntWritable();
	private IntWritable tar= new IntWritable();
	public void map(Object key, Text value, Context context) throws IOException, InterruptedException{
	String[] line=value.toString().split("\\s+");
	src.set(Integer.parseInt(line[0]));
	tar.set(Integer.parseInt(line[1]));
	context.write(src,new IntWritable(1));
    context.write(tar,new IntWritable(-1));
    }
  }

public static class FirstReducer extends Reducer<IntWritable,IntWritable,IntWritable,IntWritable>{
    private IntWritable node_id = new IntWritable();
    private IntWritable difference = new IntWritable();
	  public void reduce(IntWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException{
        int diff=0;
        for (IntWritable i: values)
        {
        diff=diff+i.get();
		    }
        node_id.set(key.get());
        difference.set(diff);
        context.write(node_id,difference);   
	  }
  }
    
  public static class SecondMapper extends Mapper<Object,Text,IntWritable,IntWritable>{
      
      private IntWritable difference = new IntWritable();
      private IntWritable node_id= new IntWritable();
      public void map(Object key, Text value, Context context) throws IOException, InterruptedException{
          String[] line=value.toString().split("\t");
          node_id.set(Integer.parseInt(line[0]));
          difference.set(Integer.parseInt(line[1]));
          context.write(difference,node_id);
      } 
    }
    
  public static class SecondReducer extends Reducer<IntWritable,IntWritable,IntWritable,IntWritable>{
    private IntWritable difference = new IntWritable();
    private IntWritable count = new IntWritable();
    public void reduce(IntWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException{
      int count_i=0;
          for (IntWritable i: values){
          count_i=count_i+1;        
          }
      difference.set(key.get());
      count.set(count_i);
      context.write(difference,count);
      }
    }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "Q4");
    job.setJarByClass(Q4.class);
    job.setMapperClass(FirstMapper.class);
    job.setReducerClass(FirstReducer.class);
    job.setOutputKeyClass(IntWritable.class);
    job.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path("OUT_TMP"));
    job.waitForCompletion(true);
        
    Job job2 = Job.getInstance(conf, "Q4_2");
    job2.setJarByClass(Q4.class);
    job2.setMapperClass(SecondMapper.class);
    job2.setReducerClass(SecondReducer.class);
    job2.setOutputKeyClass(IntWritable.class);
    job2.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job2, new Path("OUT_TMP"));
    FileOutputFormat.setOutputPath(job2, new Path(args[1]));
    System.exit(job2.waitForCompletion(true) ? 0 : 1);
    }
}
