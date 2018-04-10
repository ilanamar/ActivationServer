package com.gc.addrs.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.gc.addrs.config.BridgeGlobals;
import com.gc.addrs.stat.BridgeStats;
import com.gc.addrs.util.CommonUtils;
import com.gc.addrs.util.Format;
import com.gc.addrs.util.SmtpAuthMailer;

/**
 * This class is the Report generator for the EpSession application.
 * The genReport method is called with an array of aggregate results
 * from each agent and it generates the output report.
 * The summary report contains a summary of the numerical quantities -
 * transaction counts, rates and response times.
 *
 */
public class BridgeStatsReporter extends Thread {

	private static final int MS_IN_ONE_MINUTE = 1000 * 60;
	
	private static final Log logger = LogFactory.getLog(ActivationServer.class);
	
	private List statsObjects;
	// private BridgeStats stats;
	private int collectInterval = 0;
	private boolean stopRequested = false;
	
	int runtime;
	
	double sumTx1Resp;
	int sumTx1Count, maxTx1Resp;
	int sumTx1RespHist[];

	double sumTx2Resp;
	int sumTx2Count, maxTx2Resp;
	int sumTx2RespHist[];
	
	double sumTx3Resp;
	int sumTx3Count, maxTx3Resp;
	int sumTx3RespHist[];
	
	double sumTxOtherResp;
	int sumTxOtherCount, maxTxOtherResp;
	int sumTxOtherRespHist[];

	public BridgeStatsReporter(int collectInterval) {
	// public BridgeStatsReporter(BridgeStats stats, int collectInterval) {
		super("BridgeStatsReporter");
		// this.stats = stats;
		this.statsObjects = new ArrayList();
		this.collectInterval = collectInterval;
	}
	
	public void clear() {
		runtime = 0;

		sumTx1Resp = 0;
		sumTx1Count = 0;
		maxTx1Resp = 0;
		sumTx1RespHist = null;
		sumTx1RespHist = new int[BridgeStats.RESPMAX];
		
		sumTx2Resp = 0;
		sumTx2Count = 0;
		maxTx2Resp = 0;
		sumTx2RespHist = null;
		sumTx2RespHist = new int[BridgeStats.RESPMAX];
		
		sumTx3Resp = 0;
		sumTx3Count = 0;
		maxTx3Resp = 0;
		sumTx3RespHist = null;
		sumTx3RespHist = new int[BridgeStats.RESPMAX];
		
		sumTxOtherResp = 0;
		sumTxOtherCount = 0;
		maxTxOtherResp = 0;
		sumTxOtherRespHist = null;
		sumTxOtherRespHist = new int[BridgeStats.RESPMAX];
	}
	
	public synchronized void addStatObject(BridgeStats statsObject) {
		statsObjects.add(statsObject);
	}
	
	public synchronized void removeStatObject(BridgeStats statsObject) {
		statsObjects.remove(statsObject);
	}

	public void run() {
		
		while (!stopRequested) {
			
			try { Thread.sleep(collectInterval * MS_IN_ONE_MINUTE); } catch (Exception e) {}
			
			for (int i = 0; i < statsObjects.size(); i++) {
				clear();
				BridgeStats stats = (BridgeStats) statsObjects.get(i);
				try {
					File file = genReport(stats);
					if (BridgeGlobals.getInstance().isEnableEmailPerformanceReports() && (file != null)) {
						emailReport(stats, file);
					}
				} catch (Exception ioe) {				
				}				
				stats.init();
			}
		}
	}

	/**
	 * @return double - txPerMin
	 */
	public File genReport(BridgeStats stats) throws IOException {
	
		if (stats.getTxTotal() == 0) {
			logger.info("Performance summary report was not generated for " + stats.getBridgeId() + ": no transcations during run interval");						
			return null;
		}

		BufferedReader bufp;

		String resultsDir = "./stats/" + stats.getBridgeId();
		new File(resultsDir).mkdirs();
		// String filesep = System.getProperty("file.separator");
		String reportFilename = resultsDir + "/" + stats.getBridgeId() + "_stats_" + CommonUtils.getSimpleDateTime() + ".txt";
		PrintStream sump = new PrintStream(new FileOutputStream(reportFilename));
		int i = 0;

		processStats(stats);

		runtime = stats.getRuntimeMs();
		double txPerMin = printSummary(stats, sump, false);
		logger.info("Printed performance summary report: " + reportFilename);			
		return (new File(reportFilename));
	}

	public void emailReport(BridgeStats stat, File reportFile) throws Exception {

		SmtpAuthMailer mailer = new SmtpAuthMailer(
				BridgeGlobals.getInstance().getSmptHost(), 
				BridgeGlobals.getInstance().getSmtpUsername(), 
				BridgeGlobals.getInstance().getSmtpPassword());
	
		StringBuffer msg = new StringBuffer();
		msg.append("\n");
		msg.append("Attached, please find, performane report for: ");
		msg.append(stat.getBridgeId());;
		msg.append("\n");

		mailer.sendMail(
				BridgeGlobals.getInstance().getAlertFrom(), 
				BridgeGlobals.getInstance().getStatRecipients(), 
				BridgeGlobals.getInstance().getStatSubject(), 
				msg.toString(),
				reportFile);		
		
	}
	
	private void processStats(BridgeStats stats) {
		
		int j;

		sumTx1Count += stats.txCnt[BridgeStats.TX_1];
		sumTx1Resp += stats.respSum[BridgeStats.TX_1];
		if (stats.respMax[BridgeStats.TX_1] > maxTx1Resp)
			maxTx1Resp = stats.respMax[BridgeStats.TX_1];

		sumTx2Count += stats.txCnt[BridgeStats.TX_2];
		sumTx2Resp += stats.respSum[BridgeStats.TX_2];
		if (stats.respMax[BridgeStats.TX_2] > maxTx2Resp)
			maxTx2Resp = stats.respMax[BridgeStats.TX_2];

		sumTx3Count += stats.txCnt[BridgeStats.TX_3];
		sumTx3Resp += stats.respSum[BridgeStats.TX_3];
		if (stats.respMax[BridgeStats.TX_3] > maxTx3Resp)
			maxTx3Resp = stats.respMax[BridgeStats.TX_3];

		sumTxOtherCount += stats.txCnt[BridgeStats.TX_OTHER];
		sumTxOtherResp += stats.respSum[BridgeStats.TX_OTHER];
		if ( stats.respMax[BridgeStats.TX_OTHER] > maxTxOtherResp)
			maxTxOtherResp = stats.respMax[BridgeStats.TX_OTHER];

		/* Now get the histogram data */
		for (j = 0; j < BridgeStats.RESPMAX; j++)
			sumTx1RespHist[j] += stats.respHist[BridgeStats.TX_1][j];

		for (j = 0; j < BridgeStats.RESPMAX; j++)
			sumTx2RespHist[j] += stats.respHist[BridgeStats.TX_2][j];

		for (j = 0; j < BridgeStats.RESPMAX; j++)
			sumTx3RespHist[j] += stats.respHist[BridgeStats.TX_3][j];

		for (j = 0; j < BridgeStats.RESPMAX; j++)
			sumTxOtherRespHist[j] += stats.respHist[BridgeStats.TX_OTHER][j];

	}

	// Print summary report
	private double printSummary(BridgeStats stat, PrintStream p, boolean isHTML) {

		String nl = isHTML ? "\n<br>" : "\n";

		String bridgeId = stat.getBridgeId();
		String[] trxNames = stat.getTrxTypeNames();
		double txcnt = 0, txPerMin = 0;
		double Tx1Per = 0, Tx2Per = 0, Tx3Per = 0, TxOtherPer = 0;
		boolean success = true;
		double avg, tavg, resp90;
		int sumtx, cnt90;
		boolean fail90 = false, failavg =false;
		int i;
		String passStr = null;

    	p.println();
    	p.println("\t\t\tPerformace Report for " + bridgeId);
    	p.println();

    	long reportStartTs = stat.getTimer().getOffsetTime();
    	long reportEndTs = stat.getTimer().getOffsetTime() + stat.getRuntimeMs();
		p.println("Report Period Start:\t" + new Date(reportStartTs));
		p.println("Report Period End:\t" + new Date(reportEndTs));
		p.println("Report Duration:\t" + CommonUtils.duration(stat.getRuntimeMs()));
    	p.println();

		txcnt = sumTx1Count + sumTxOtherCount;

   	 	if (txcnt > 0) {
      		txPerMin = (txcnt * 1000 * 60) / runtime;
      		Tx1Per = (sumTx1Count*100) / txcnt;
      		Tx2Per = (sumTx2Count*100) / txcnt;
      		Tx3Per = (sumTx3Count*100) / txcnt;
      		TxOtherPer = (sumTxOtherCount*100) / txcnt;
        }
		p.println("Total Number of Transactions = " + (int)txcnt);
		p.print("Transaction Rate: ");
		Format.print(p, "%.02f tx/min", txPerMin);
		p.println();
		logger.info(bridgeId + " tx/min = " + new Format("%.02f").form(txPerMin));
		p.println();
		p.println();
		p.println("TRANSACTION MIX\n");
		p.println();
		p.println      ("TRANSACTION TYPE\tTX. COUNT\tMIX");
		p.println      ("----------------\t---------\t---");
		Format.print(p, "%-17s\t", trxNames[BridgeStats.TX_1]);
		Format.print(p, "%05d\t\t", sumTx1Count); Format.print(p, "%5.02f%\n", Tx1Per);
		Format.print(p, "%-17s\t", trxNames[BridgeStats.TX_2]);
		Format.print(p, "%05d\t\t", sumTx2Count); Format.print(p, "%5.02f%\n", Tx2Per);
		Format.print(p, "%-17s\t", trxNames[BridgeStats.TX_3]);
		Format.print(p, "%05d\t\t", sumTx3Count); Format.print(p, "%5.02f%\n", Tx3Per);
		Format.print(p, "%-17s\t", trxNames[BridgeStats.TX_OTHER]);
		Format.print(p, "%05d\t\t", sumTxOtherCount);
		Format.print(p, "%5.02f%\n", TxOtherPer);

		/* Compute response time info */
		p.println();
		p.println();
		p.println          ("RESPONSE TIMES \t\tAVG.\t\tMAX.\t\t90TH%");
		p.println          ("-------------- \t\t----\t\t----\t\t-----");
		if (sumTx1Count > 0) {
			avg  = (sumTx1Resp/sumTx1Count) / 1000;
			sumtx = 0;
			cnt90 = (int)(sumTx1Count * .90);
			for (i = 0; i < BridgeStats.RESPMAX; i++) {
				sumtx += sumTx1RespHist[i];
				if (sumtx >= cnt90)		/* 90% of tx. got */
					break;
			}
			resp90 = (i + 1) * BridgeStats.RESPUNIT;

			Format.print(p, "%-14s\t\t", trxNames[BridgeStats.TX_1]);
			Format.print(p, "%.03f\t\t", avg);
			Format.print(p, "%.03f\t\t", (double)maxTx1Resp/1000);
			Format.print(p, "%.03f\n", resp90);
		}
		else {
			Format.print(p, "%-14s\t\t", trxNames[BridgeStats.TX_1]);
			p.println("0.000\t\t0.000\t\t0.000");
		}

		if (sumTx2Count > 0) {
			avg  = (sumTx2Resp/sumTx2Count) / 1000;
			sumtx = 0;
			cnt90 = (int)(sumTx2Count * .90);
			for (i = 0; i < BridgeStats.RESPMAX; i++) {
				sumtx += sumTx2RespHist[i];
				if (sumtx >= cnt90)		/* 90% of tx. got */
					break;
			}
			resp90 = (i + 1) * BridgeStats.RESPUNIT;

			Format.print(p, "%-14s\t\t", trxNames[BridgeStats.TX_2]);
			Format.print(p, "%.03f\t\t", avg);
			Format.print(p, "%.03f\t\t", (double)maxTx2Resp/1000);
			Format.print(p, "%.03f\n", resp90);
		}
		else {
			Format.print(p, "%-14s\t\t", trxNames[BridgeStats.TX_2]);
			p.println("0.000\t\t0.000\t\t0.000");
		}

		if (sumTx2Count > 0) {
			avg  = (sumTx3Resp/sumTx3Count) / 1000;
			sumtx = 0;
			cnt90 = (int)(sumTx3Count * .90);
			for (i = 0; i < BridgeStats.RESPMAX; i++) {
				sumtx += sumTx3RespHist[i];
				if (sumtx >= cnt90)		/* 90% of tx. got */
					break;
			}
			resp90 = (i + 1) * BridgeStats.RESPUNIT;

			Format.print(p, "%-14s\t\t", trxNames[BridgeStats.TX_3]);
			Format.print(p, "%.03f\t\t", avg);
			Format.print(p, "%.03f\t\t", (double)maxTx3Resp/1000);
			Format.print(p, "%.03f\n", resp90);
		}
		else {
			Format.print(p, "%-14s\t\t", trxNames[BridgeStats.TX_3]);
			p.println("0.000\t\t0.000\t\t0.000");
		}

		if (sumTxOtherCount > 0) {
			avg  = (sumTxOtherResp/sumTxOtherCount) / 1000;
			sumtx = 0;
			cnt90 = (int)(sumTxOtherCount * .90);
			for (i = 0; i < BridgeStats.RESPMAX; i++) {
				sumtx += sumTxOtherRespHist[i];
				if (sumtx >= cnt90)		/* 90% of tx. got */
					break;
			}
			resp90 = (i + 1) * BridgeStats.RESPUNIT;
			Format.print(p, "%-14s\t\t", trxNames[BridgeStats.TX_OTHER]);
			Format.print(p, "%.03f\t\t", avg);
			Format.print(p, "%.03f\t\t", (double)maxTxOtherResp/1000);
			Format.print(p, "%.03f\n", resp90);
		}
		else {
			Format.print(p, "%-14s\t\t", trxNames[BridgeStats.TX_OTHER]);
			p.println("0.000\t\t0.000\t\t0.000");
		}

		return(txPerMin);
	}

	public void interrupt() {
		logger.info("BridgeStatReporter received stop request...");
		stopRequested = true;
		super.interrupt();
	}
}
