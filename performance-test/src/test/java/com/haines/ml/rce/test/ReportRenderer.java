package com.haines.ml.rce.test;

import java.awt.Dimension;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultXYDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.haines.ml.rce.test.ReportGenerator.Report;

public interface ReportRenderer {
	
	public final static Util UTIL = new Util();
	
	void render(Iterable<Report> reports) throws IOException;
	
	public static class SLF4JReportRenderer implements ReportRenderer {

		private static final Logger LOG = LoggerFactory.getLogger(SLF4JReportRenderer.class);
		
		@Override
		public void render(Iterable<Report> reports) {
			
			for (Report report: reports){
				LOG.info("\n\n\t\t\t----------------------------Report "+report.getReportName()+" completed-----------------------------\n"+
					     "\t\t\t| classifier accuracy: "+report.getAccuracy()+"\t\t\t\t|\n" +
					     "\t\t\t| classifier fmeasure: "+report.getFmeasure()+"\t\t\t\t\t\t|\n" +
					     "\t\t\t| classifier roc: "+report.getRoc()+"\t\t\t\t\t\t\t|\n" +
					     "\t\t\t| classifier model Size: "+report.getNumBytesUsedForModel()+"\t\t\t\t\t|\n" +
						 "\t\t\t----------------------------Report completed-----------------------------\n\n");
			}
		}
	}
	
	public static class Util {
		
		private Util(){}
		
		public ReportRenderer chain(ReportRenderer... renderers){
			return new MultipleReportRenderer(Lists.newArrayList(renderers));
		}
		
		private static class MultipleReportRenderer implements ReportRenderer {

			private final Iterable<ReportRenderer> renderers;
			
			private MultipleReportRenderer(Iterable<ReportRenderer> renderers){
				this.renderers = renderers;
			}
			
			@Override
			public void render(Iterable<Report> reports) throws IOException {
				for (ReportRenderer renderer: renderers){
					renderer.render(reports);
				}
			}
		}
	}
	
	public static class JPanelJChartROCRenderer implements ReportRenderer{

		@Override
		public void render(Iterable<Report> reports) throws IOException {
			ChartPanel panel = new ChartPanel(JChartROCRenderer.getChart(reports));
			
			Dimension bounds = new Dimension(560 , 367);
			panel.setPreferredSize(bounds);
			
			JFrame frame = new JFrame("ROC Chart");
			
			frame.setContentPane(panel);
			frame.setBounds(0, 0, 800, 600);
			
			frame.setVisible(true);
		}
	}
	
	public static class JChartROCRenderer implements ReportRenderer {

		private final OutputStream out;
		private final int width;
		private final int height;
		
		public JChartROCRenderer(OutputStream out, int width, int height){
			this.out = out;
			this.width = width;
			this.height = height;
		}
		
		@Override
		public void render(Iterable<Report> reports) throws IOException {

			ChartUtilities.writeChartAsPNG(out, getChart(reports), width, height);
		}
		
		private static JFreeChart getChart(Iterable<Report> reports){
			DefaultXYDataset dataset = new DefaultXYDataset();
			
			for (Report report: reports){
				double[][] rocData = zeroBased(report.getRocData());
				for (int i = 0; i < rocData.length; i++){
					dataset.addSeries(report.getReportName(), rocData);
				}
			}
			
			return ChartFactory.createXYLineChart("ROC", "FPR", "TPR", dataset, PlotOrientation.VERTICAL, !Iterables.isEmpty((Iterables.skip(reports, 1))), false, false);
		}

		private static double[][] zeroBased(double[][] rocData) {
			
			double[] fpr = rocData[0];
			double[] tpr = rocData[1];
			
			if (tpr[tpr.length - 1] != 0 || fpr[fpr.length - 1] != 0){
				double[] tmptpr = new double[tpr.length + 1];
				double[] tmpfpr = new double[fpr.length + 1];
				
				System.arraycopy(tpr, 0, tmptpr, 0, tpr.length);
				System.arraycopy(fpr, 0, tmpfpr, 0, fpr.length);
				
				tmptpr[tpr.length] = 0;
				tmpfpr[fpr.length] = 0;
				
				tpr = tmptpr;
				fpr = tmpfpr;
				
				return new double[][]{fpr, tpr};
				
			} else{
				return rocData;
			}
		}
	}
}
