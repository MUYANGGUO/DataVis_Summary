package edu.gatech.cse6242;

import java.io.IOException;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.util.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Q1 {
  
	public static class TokenizerMapper extends Mapper<Object, Text, Text, Text>{
    private Text src= new Text();
		private Text output = new Text();
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

			String[] Token= value.toString().split("\t");
			if (Token.length == 3) {
				src.set(Token[0]);
				output.set(Token[1] + "," + Token[2]);
				context.write(src,output);
			}
		}
	}

	public static class TokenizerReducer extends Reducer<Text,Text,Text,Text> {
    	private Text reduced_output = new Text();
    	public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
    		int max = Integer.MIN_VALUE;
    		int target_max = Integer.MIN_VALUE;
    		for (Text val : values) {
    			String[] Token= val.toString().split(",");
    			if (max < Integer.parseInt(Token[1])) {
    				max = Integer.parseInt(Token[1]);
    				target_max = Integer.parseInt(Token[0]);
    			}
    		}
    		reduced_output.set(Integer.toString(target_max) + "," + Integer.toString(max));
    		context.write(key, reduced_output);
    	}
    }

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "Q1");
		/* TODO: Needs to be implemented */
		job.setJarByClass(Q1.class);
		job.setMapperClass(TokenizerMapper.class);
		job.setCombinerClass(TokenizerReducer.class);
		job.setReducerClass(TokenizerReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
