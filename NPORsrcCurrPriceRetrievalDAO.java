// ==========================================================================
//                     Copyright 2007-2009, JDA Software Group, Inc.
//                             All Rights Reserved
//
//                THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF
//                              JDA Software Group, Inc.
//
//
//      The copyright notice above does not evidence any actual
//      or intended publication of such source code.
//
// ==========================================================================
/** DAO used for retrieving
 * Rsrc Current price from NPO_RESRC_DT
 *  @author Sowjanya Thangellapalli
 *  @author Last updated by $Author: sravindran $
 *  @version $Revision: 1.2.68.2.6.2 $
 */
package com.manu.scpoweb.npo.process.optimize.dao.retrieval;

import com.manu.gensys.common.SCPOTimer;
import com.manu.scpoweb.common.dao.pipeline.RetrievalDAO;
import com.manu.scpoweb.npo.process.optimize.algs.NPOAction;
import com.manu.scpoweb.npo.process.optimize.common.NPOProcessConstants;
import com.manu.webservices.common.Trace;
import com.manu.webservices.db.ManuDBException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NPORsrcCurrPriceRetrievalDAO extends RetrievalDAO
{
    public static final String DAO_NAME = "com.manu.scpoweb.npo.process.optimize.dao.retrieval.NPORsrcCurrPriceRetrievalDAO";

    protected String getLoadForBatchStmt(Object contextInfo)
    {
        return null;
    }

    /**
     * method for
     * building the query
     *
     * @param processID
     * @return
     */
    protected String getLoadForBatchStmt(long processID)
    {
        StringBuffer query = null;
        query = new StringBuffer("SELECT DISTINCT RSRCDT.RESRC_ID,RSRCDT.STARTDATE,RSRCDT.CURRENT_PRICE,RSRCDT.CURRENT_BKD,RSRCDT.SIM_BKD_CURRENT_PRICE_UNCONS, ");
        query.append(" RSRCDT.REF_PRICE,RSRCDT.REF_PRICE_LOW,RSRCDT.REF_PRICE_HIGH,RSRCDT.CURRENT_PRICE_AVAIL_SW,RSRCDT.AVAILABILITY,RSRCDT.LIVE_PRICE, RSRCDT.AUX_CAP, ");
        query.append(" RSRCDT.REC_PRICE, RSRCDT.OVERRIDE_PRICE");
        query.append(" FROM PROCESSNPO PNPO,NPO_RESRC_DT RSRCDT ");
        query.append(" WHERE PNPO.PROCESSID = ? ");
        query.append(" AND PNPO.ACTION_NUMBER = ? ");
        query.append(" AND PNPO.RSRC_ID = RSRCDT.RESRC_ID");
        query.append(" AND PNPO.RSRC_STARTDATE = RSRCDT.STARTDATE ");
        query.append(" ORDER BY RSRCDT.RESRC_ID,RSRCDT.STARTDATE ");
        return query.toString();
    }


    /**
     * method to execute the query
     * and getting the
     * rsrc curr price depending
     * on process id
     *
     * @param processID
     * @param connection
     * @return
     * @throws ManuDBException
     */
    public Map<String, Map> loadForBatch(long processID, NPOAction npoAction, Connection connection) throws ManuDBException
    {
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        String rsrcPrimaryKey = null;
        Map<String, Double> currPriceMap = new HashMap<String, Double>();
        Map<String, Double> currBkdMap = new HashMap<String, Double>();
        Map<String, Double> currPriceUnconsMap = new HashMap<String, Double>();
        Map<String, Double> refPriceMap = new HashMap<String, Double>();
        Map<String, Double> refPriceLowMap = new HashMap<String, Double>();
        Map<String, Double> refPriceHighMap = new HashMap<String, Double>();
        Map<String, Double> availabilityMap = new HashMap<String, Double>();
        Map<String, String> currentPriceAvailSwMap = new HashMap<String, String>();
        Map<String, Double> livePriceMap = new HashMap<String, Double>();
        Map<String, Double> auxCapacityMap = new HashMap<String, Double>();
        Map<String, Double> prevRecPriceMap = new HashMap<String, Double>();
        Map<String, Double> prevOverridePriceMap = new HashMap<String, Double>();

        Map<String, Map> dataSetMap = new HashMap<String, Map>();
        try
        {
            String loadQuery = getLoadForBatchStmt(processID);
            pstmt = connection.prepareStatement(loadQuery,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);//connection.prepareStatement(loadQuery);
            pstmt.setLong(1, processID);
            pstmt.setLong(2, npoAction.getActionNumber());

            if (Trace.isVerbose())
            {
                Trace.write(Trace.informational, loadQuery);
                Trace.write(Trace.informational, " Query Parameters ");
                Trace.write(Trace.informational, " PNPO.PROCESSID = " + processID);
                Trace.write(Trace.informational, " PNPO.ACTION_NUMBER = " + npoAction.getActionNumber());
            }

            rs = pstmt.executeQuery();
            SCPOTimer timer = new SCPOTimer();
            Trace.write(Trace.error, "  - "+ "Population Old");
            Trace.write(Trace.error, "  - "+ System.currentTimeMillis());
            while (rs.next())
            {
                rsrcPrimaryKey = rs.getString(1) + ":" + rs.getDate(2);
                currPriceMap.put(rsrcPrimaryKey, rs.getDouble(3));
                currBkdMap.put(rsrcPrimaryKey, rs.getDouble(4));
                currPriceUnconsMap.put(rsrcPrimaryKey, rs.getDouble(5));
                refPriceMap.put(rsrcPrimaryKey, rs.getDouble(6));
                refPriceLowMap.put(rsrcPrimaryKey, rs.getDouble(7));
                refPriceHighMap.put(rsrcPrimaryKey, rs.getDouble(8));
                currentPriceAvailSwMap.put(rsrcPrimaryKey, rs.getString(9));
                availabilityMap.put(rsrcPrimaryKey, rs.getDouble(10));
                livePriceMap.put(rsrcPrimaryKey, rs.getDouble(11));
                auxCapacityMap.put(rsrcPrimaryKey, rs.getDouble(12));
                prevRecPriceMap.put(rsrcPrimaryKey, rs.getDouble(13));
                prevOverridePriceMap.put(rsrcPrimaryKey, rs.getDouble(14));
            }
            Trace.write(Trace.error, "  - "+ System.currentTimeMillis());
            rs.first();
            class Record
            {
            	
				String rsrcPrimaryKey ;
                double currPrice;
                double currBkd;
                double currPriceUncons;
                double refPrice;
                double refPriceLow;
                double refPriceHigh;
                String currentPriceAvailSw;
                double availability;
                double livePrice;
                double auxCapacity;
                double prevRecPrice;
                double prevOverridePrice;
                public Record(ResultSet td)
				{
                	try {
					this.rsrcPrimaryKey = td.getString(1) + ":" + td.getDate(2);
					this.currPrice=td.getDouble(3);
					this.currBkd=( td.getDouble(4));
					this.currPriceUncons= td.getDouble(5);
					this.refPrice= td.getDouble(6);
					this.refPriceLow= td.getDouble(7);
					this.refPriceHigh=td.getDouble(8);
					this.currentPriceAvailSw= td.getString(9);
					this.availability= td.getDouble(10);
					this.livePrice=td.getDouble(11);
					this.auxCapacity= td.getDouble(12);
					this.prevRecPrice= td.getDouble(13);
					this.prevOverridePrice= td.getDouble(14);
                	}
					catch (SQLException e) {
						Trace.write(Trace.error,"some sql error");
					}
				}
            }
            Map<String, Record > table =new HashMap<String, Record>();
            Trace.write(Trace.error, "  - "+ "Population New");
            Trace.write(Trace.error, "  - "+ System.currentTimeMillis());
            try {
				while(rs.next())
				{
					Record currentRecord = new Record(rs);
					table.put(currentRecord.rsrcPrimaryKey , currentRecord);
					/*Use CurrentRecord HashCode for More Performance like table.put(currentRecord, currentRecord); Gives more readability */
				}
				rs.first();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
            Trace.write(Trace.error, "  - "+ System.currentTimeMillis());
            Set<String> keys1 = null;
            keys1=table.keySet();
            Set<String> keys2 =null;
            keys2=table.keySet();
            Object[] array1 = keys1.toArray();
            Object[] array2 = keys2.toArray();
            Record curRecord;
            Object temp;
            Trace.write(Trace.error, "  - "+ "Retrival New");
            Trace.write(Trace.error, "  - "+ System.currentTimeMillis());
            for(int j=0;j<50;j++)
            for(int i=0; i<array1.length; i++)
            {
            curRecord=table.get((String)array1[i]);
            temp=curRecord.auxCapacity;
            temp=curRecord.availability;
            temp=curRecord.currBkd;
            temp=curRecord.currentPriceAvailSw;
            temp=curRecord.currPrice;
            temp=curRecord.currPriceUncons;
            temp=curRecord.livePrice;
            temp=curRecord.prevOverridePrice;
            temp=curRecord.prevRecPrice;
            temp=curRecord.refPrice;
            temp=curRecord.refPriceHigh;
            temp=curRecord.refPriceLow;
            //temp=curRecord.rsrcPrimaryKey;
            }
            Trace.write(Trace.error, "  - "+ System.currentTimeMillis());
            dataSetMap.put(NPOProcessConstants.RSRC_CURRENT_PRICE_MAP, currPriceMap);
            dataSetMap.put(NPOProcessConstants.RSRC_CURRENT_BOOKED_MAP, currBkdMap);
            dataSetMap.put(NPOProcessConstants.RSRC_CURRENT_PRICE_UNCONS, currPriceUnconsMap);
            dataSetMap.put(NPOProcessConstants.RSRC_REF_PRICE, refPriceMap);
            dataSetMap.put(NPOProcessConstants.RSRC_REF_PRICE_LOW, refPriceLowMap);
            dataSetMap.put(NPOProcessConstants.RSRC_REF_PRICE_HIGH, refPriceHighMap);
            dataSetMap.put(NPOProcessConstants.RSRC_AVAILABILITY, availabilityMap);
            dataSetMap.put(NPOProcessConstants.RSRC_CURRENT_PRICE_AVAIL_SW, currentPriceAvailSwMap);
            dataSetMap.put(NPOProcessConstants.RSRC_LIVE_PRICE, livePriceMap);
            dataSetMap.put(NPOProcessConstants.RSRC_AUX_CAPACITY, auxCapacityMap);
            dataSetMap.put(NPOProcessConstants.RSRC_PREV_REC_PRICE, prevRecPriceMap);
            dataSetMap.put(NPOProcessConstants.RSRC_PREV_OVERRIDE_PRICE, prevOverridePriceMap);
            Trace.write(Trace.error, "  - "+ "Retrival old");
            Trace.write(Trace.error, "  - "+ System.currentTimeMillis());
            for(int j=0;j<50;j++)
            for(int i=0; i<array2.length; i++)
            {
            	rsrcPrimaryKey=(String)array2[i];
            	temp=dataSetMap.get(NPOProcessConstants.RSRC_CURRENT_PRICE_MAP).get(rsrcPrimaryKey);
            	temp=dataSetMap.get(NPOProcessConstants.RSRC_CURRENT_BOOKED_MAP).get(rsrcPrimaryKey);
            	temp=dataSetMap.get(NPOProcessConstants.RSRC_CURRENT_PRICE_UNCONS).get(rsrcPrimaryKey);
            	temp=dataSetMap.get(NPOProcessConstants.RSRC_REF_PRICE).get(rsrcPrimaryKey);
            	temp=dataSetMap.get(NPOProcessConstants.RSRC_REF_PRICE_LOW).get(rsrcPrimaryKey);
            	temp=dataSetMap.get(NPOProcessConstants.RSRC_REF_PRICE_HIGH).get(rsrcPrimaryKey);
            	temp=dataSetMap.get(NPOProcessConstants.RSRC_AVAILABILITY).get(rsrcPrimaryKey);
            	temp=dataSetMap.get(NPOProcessConstants.RSRC_CURRENT_PRICE_AVAIL_SW).get(rsrcPrimaryKey);
            	temp=dataSetMap.get(NPOProcessConstants.RSRC_LIVE_PRICE).get(rsrcPrimaryKey);
            	temp=dataSetMap.get(NPOProcessConstants.RSRC_AUX_CAPACITY).get(rsrcPrimaryKey);
            	temp=dataSetMap.get(NPOProcessConstants.RSRC_PREV_REC_PRICE).get(rsrcPrimaryKey);
            	temp=dataSetMap.get(NPOProcessConstants.RSRC_PREV_OVERRIDE_PRICE).get(rsrcPrimaryKey);
//            	temp=currPriceMap.get(rsrcPrimaryKey);
//            	temp=availabilityMap.get(rsrcPrimaryKey);
//            	temp=currBkdMap.get(rsrcPrimaryKey);
//            	temp=auxCapacityMap.get(rsrcPrimaryKey);
//            	temp=currentPriceAvailSwMap.get(rsrcPrimaryKey);
//            	temp=refPriceLowMap.get(rsrcPrimaryKey);
//            	temp=currPriceUnconsMap.get(rsrcPrimaryKey);
//            	temp=livePriceMap.get(rsrcPrimaryKey);
//            	temp=prevOverridePriceMap.get(rsrcPrimaryKey);
//            	temp=prevRecPriceMap.get(rsrcPrimaryKey);
//            	temp=refPriceMap.get(rsrcPrimaryKey);
//            	temp=refPriceHighMap.get(rsrcPrimaryKey);
            }
            Trace.write(Trace.error, "  - "+ System.currentTimeMillis());
        }
        catch (SQLException se)
        {
            Trace.write(Trace.error, " SQLException during retrieval of dfu data At: com.manu.scpoweb.npo.process.optimize.dao.retrieval.NPORsrcCurrPriceRetrievalDAO - loadForBatch(long, NPOAction, Connection) - ", se);
            ManuDBException manuDBException = new ManuDBException("DRM.Normalization.Exception.RsrcCurrPriceRetrievalError", se);
            throw manuDBException;
        }
        finally
        {
            close(rs, pstmt);
        }


        return dataSetMap;
    }
}
/**
 * $Log: NPORsrcCurrPriceRetrievalDAO.java,v $
 * Revision 1.2.68.2.6.2  2012/09/11 11:00:55  sravindran
 * Modified to make opt publish previous Rec and Override price to NpoResrcDt and hist and NpoDfudtRec and hist tables
 *
 * Revision 1.2.68.2.6.1  2012/05/24 02:47:30  sduraira
 * Post release code-formatting and optimization of imports
 *
 * Revision 1.2.68.2  2011/06/17 12:51:51  pdronadula
 * Retrieved Aux_cap from NPO_RESRC_DT table and Calculated aux_utilization and populating it to RESRC_DT table.
 *
 * Revision 1.2.68.1  2011/01/24 11:06:37  aanumolu
 * Merged TPO 7.6 changes to 7.7 branch.
 *
 * Revision 1.2.12.2  2010/12/08 05:10:08  rbadveeti
 * Exception Handling Related Changes
 *
 * Revision 1.2.12.1  2010/11/24 09:00:47  rbadveeti
 * Added Log Messages for Display SQL Query & It's Positional Parameters
 *
 * Revision 1.2  2009/08/12 14:31:38  mnalla
 * moving  from 'rel-7-4-2-0-NPO-Branch' to SCPO tip
 *
 * Revision 1.1.2.11  2009/03/10 12:35:15  pkalode
 * code added to get the values from LIVE_PRICE column.
 *
 * Revision 1.1.2.10  2008/11/10 10:11:53  pkalode
 * code added to retrieve value of refPrice, refPriceLow, refPriceHigh, availability,
 * currentPriceAvailSw.
 *
 * Revision 1.1.2.9  2008/09/15 09:44:12  sthangel
 * query changes for performance improvements.
 *
 * Revision 1.1.2.8  2008/08/28 11:22:33  j1007617
 * query changed to fetch the values of CURRENT_BKD and
 * SIM_BKD_CURRENT_PRICE_UNCONS.
 *
 * Revision 1.1.2.7  2008/08/22 14:29:41  j1007617
 * query modified according to new DB changes.
 *
 * Revision 1.1.2.6  2008/06/06 09:56:02  mnalla
 * modified query to consider changes in 'processNPO' structure
 *
 * Revision 1.1.2.5  2008/06/03 04:41:19  sthangel
 * INCREMENTAL CHANGES
 *
 * Revision 1.1.2.4  2008/06/02 12:53:01  sthangel
 * modified query to take
 * action number in to consideration.
 *
 * Revision 1.1.2.3  2008/06/02 10:27:40  mnalla
 * modified query to reflect changes in 'PROCESSNPO' table
 *
 * Revision 1.1.2.2  2008/06/02 07:19:21  sthangel
 * incremental changes
 *
 * Revision 1.1.2.1  2008/05/29 05:36:35  sthangel
 * Intial version
 *
 */