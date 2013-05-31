package com.manu.scpoweb.npo.process.cfe.dao.retrievaldao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.manu.gensys.common.Pair;
import com.manu.scpoweb.common.SCPODAO;
import com.manu.scpoweb.npo.process.optimize.beans.DfuBean;
import com.manu.scpoweb.npo.process.optimize.beans.NPOOptimizeData;
import com.manu.scpoweb.npo.process.optimize.beans.PkgBean;
import com.manu.scpoweb.npo.process.optimize.beans.RscBean;
import com.manu.scpoweb.npo.process.optimize.common.NPODFUPrimaryKey;
import com.manu.webservices.common.Trace;
import com.manu.webservices.db.ManuDBException;

/**
 * 
 * @author j1009899
 *
 */

public class CFEDFUPkgRsrcRetrievalDAO extends SCPODAO {

	private static final int INDEX_RSRC = 1;
	private static final int INDEX_RSRC_STARTDATE = 2;
	private static final int INDEX_PKG = 3;
	private static final int INDEX_PKG_STARTDATE = 4;
	private static final int INDEX_DFU = 5;
	private static final int INDEX_STARTDATE = 6;
	private static final int INDEX_USAGE = 7;
	private static final int INDEX_AUX_USAGE = 8;
	
	protected String getLoadForBatchStmt()
    {
        StringBuffer query = new StringBuffer(" SELECT CNPO.RSRC_ID, CNPO.RSRC_STARTDATE, CNPO.PKG_ID, CNPO.PKG_STARTDATE, ");
        query.append(" CNPO.NPO_DFU_ID, CNPO.DFU_STARTDATE, CNPO.USAGE, CNPO.AUX_USAGE ");
        query.append(" FROM     PROCESSCFE CNPO , NPO_RESRC RSRC ");
        query.append(" WHERE    CNPO.RSRC_ID = ? AND CNPO.RSRC_ID = RSRC.RESRC_ID ");
        query.append(" ORDER BY CNPO.RSRC_ID, CNPO.RSRC_STARTDATE, CNPO.PKG_ID, CNPO.PKG_STARTDATE,CNPO.NPO_DFU_ID, CNPO.DFU_STARTDATE ");
        return query.toString();
    }
	
	/**
     * Load the data for the action
     *
     * @param processID process ID
     * @param action    CalcModelAction object
     * @return A map of price data (as a List of Pair for all the timeperiods) keyed by DFUPriceParamPrimaryKey
     * @throws com.manu.webservices.db.ManuDBException
     *
     */
    public NPOOptimizeData loadForBatch(String resrcId) throws ManuDBException
    {
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        Connection conn = null;

        try
        {
            // Slotting has been done; go ahead and fetch the data from various tables

            String loadQuery = getLoadForBatchStmt();
            
            conn = getConnection();
            pstmt = conn.prepareStatement(loadQuery);
            pstmt.setString(1, resrcId);

            if (Trace.isVerbose())
            {
                Trace.write(Trace.informational, loadQuery);
                Trace.write(Trace.informational, " Query Parameters ");
                Trace.write(Trace.informational, " CNPO.RESRCID = " + resrcId);
            }
            rs = pstmt.executeQuery();
            rs.setFetchSize(2000);
            Trace.write(Trace.informational, "Reader End Time: " + System.currentTimeMillis());
            
            return loadOptimizeDataSet(rs);

        }
        catch (SQLException se)
        {
            // Add logging
            Trace.write(Trace.error, "Error during retrieval of dfu data : com.manu.scpoweb.npo.process.optimize.dao.retrieval.NPODFUPkgRsrcRetrievalDAO - loadForBatch(long, Action, Connection) ", se);
            ManuDBException manuDBException =
                    new ManuDBException("DRM.Normalization.Exception.EffPriceRetrievalError", se);
            throw manuDBException;
        }
        finally
        {
            close(conn, pstmt, rs);
        }
    }

    private NPOOptimizeData loadOptimizeDataSet(ResultSet rs) throws SQLException
    {
        Map dataSet = new HashMap();
        java.sql.Date startDate = null;
        String rsrcId = null;
        String pkgId = null;
        String dfuId = null, oldDfuId = null;
        int pkgUsage = 0;
        float pkgAuxUsage = 0;
        Pair rscPkgUsage = null;
        List rscPkgUsageList = new ArrayList();
        Date rsrcStartdate = null;
        Date pkgStartdate = null;
        Date dfuStartdate = null, oldDfuStartdate = null;
        String rsrc = null, oldRsrc = null;
        String pkg = null, oldPkg = null;
        String dfu = null, oldDFU = null;
        List<RscBean> rsrcList = new ArrayList();
        List<String> pkgList = new ArrayList();
        List<String> dfuList = new ArrayList();
        List<NPODFUPrimaryKey> dfuKeyList = new ArrayList();
        List<PkgBean> pkgBeanList = new ArrayList();
        List<DfuBean> dfuBeanList = new ArrayList();
        List tempRsrcList = new ArrayList();
        List tempPkgList = new ArrayList();
        List tempDFUPkgList = new ArrayList();
        List tempPkgDFUList = new ArrayList();
        List tempRsrcDFUList = new ArrayList();
        List tempPkgRsrcList = new ArrayList();
        Map<String, List> rsrcPkgMap = new HashMap();
        Map<String, List> pkgRsrcMap = new HashMap();             // Map for storing resources to a particular pkg i.e. rsrcIndex.
        Map<String, List> rsrcDFUMap = new HashMap();
        Map<String, List> PkgDFUMap = new HashMap();
        Map<String, List> dfuPkgMap = new HashMap();
        Map<String, List> rscPkgUsageMap = new HashMap();
        NPODFUPrimaryKey key = null;
        DfuBean dfuBean = null;
        PkgBean pkgBean = null;
        RscBean rsrcBean = null;

        NPOOptimizeData npoOptimizeData = new NPOOptimizeData();

        while (rs.next())
        {
            rsrcId = rs.getString(INDEX_RSRC);
            rsrcStartdate = rs.getDate(INDEX_RSRC_STARTDATE);
            pkgId = rs.getString(INDEX_PKG);
            pkgStartdate = rs.getDate(INDEX_PKG_STARTDATE);
            dfuId = rs.getString(INDEX_DFU);
            dfuStartdate = rs.getDate(INDEX_STARTDATE);
            pkgUsage = rs.getInt(INDEX_USAGE);
            pkgAuxUsage = rs.getFloat(INDEX_AUX_USAGE);

            rsrc = rsrcId + ":" + rsrcStartdate;
            pkg = pkgId + ":" + pkgStartdate;
            dfu = dfuId + ":" + dfuStartdate;

            if ((oldDFU != null) && (!oldDFU.equals(dfu)))
            {

                if (!dfuList.contains(oldDFU))
                {
                    dfuBean = new DfuBean();
                    dfuBean.setDfuName(oldDFU);
                    dfuBeanList.add(dfuBean);
                    dfuList.add(oldDFU);
                    key = new NPODFUPrimaryKey(oldDfuId, oldDfuStartdate);
                    dfuKeyList.add(key);
                    dfuPkgMap.put(oldDFU, tempDFUPkgList);
                }
                tempDFUPkgList = new ArrayList();
            }

            if ((oldPkg != null) && (!oldPkg.equals(pkg)))
            {

                if (!pkgList.contains(oldPkg))
                {
                    pkgBean = new PkgBean();
                    pkgBean.setPkgName(oldPkg);
                    pkgBean.setUsage(pkgUsage);
                    pkgBean.setAuxUsage(pkgAuxUsage);
                    pkgBeanList.add(pkgBean);
                    pkgList.add(oldPkg);
                    PkgDFUMap.put(oldPkg, tempPkgDFUList);
                    pkgRsrcMap.put(oldPkg, tempPkgRsrcList);

                }
                tempPkgDFUList = new ArrayList();
                tempPkgRsrcList = new ArrayList();
            }

            if (pkgList.contains(pkg))
            {
                List oldRsrcList;
                oldRsrcList = pkgRsrcMap.get(pkg);
                if (!oldRsrcList.contains(rsrc))
                {
                    oldRsrcList.add(rsrc);
                }

                List oldDFUList;
                oldDFUList = PkgDFUMap.get(pkg);
                if (!oldDFUList.contains(dfu))
                {
                    oldDFUList.add(dfu);
                }
            }

            if (dfuList.contains(dfu))
            {
                List oldDFUList;
                oldDFUList = dfuPkgMap.get(dfu);
                if (!oldDFUList.contains(pkg))
                {
                    oldDFUList.add(pkg);
                }
            }


            if ((oldRsrc != null) && (!oldRsrc.equals(rsrc)))
            {

                rsrcBean = new RscBean();
                rsrcBean.setRsrcName(oldRsrc);
                rsrcList.add(rsrcBean);
                rsrcPkgMap.put(oldRsrc, tempPkgList);
                rscPkgUsageMap.put(oldRsrc, rscPkgUsageList);
                rsrcDFUMap.put(oldRsrc, tempRsrcDFUList);
                tempPkgList = new ArrayList();
                tempRsrcDFUList = new ArrayList();
                rscPkgUsageList = new ArrayList();
            }

            oldRsrc = rsrc;
            oldPkg = pkg;
            oldDFU = dfu;
            oldDfuId = dfuId;
            oldDfuStartdate = dfuStartdate;

            if (!tempRsrcDFUList.contains(oldDFU))
            {
                tempRsrcDFUList.add(oldDFU);
            }

            if (!tempPkgDFUList.contains(oldDFU))
            {
                tempPkgDFUList.add(oldDFU);
            }

            if (!tempPkgList.contains(oldPkg))
            {
                tempPkgList.add(oldPkg);
                rscPkgUsage = new Pair(pkg, (double)pkgUsage);
                rscPkgUsageList.add(rscPkgUsage);
            }

            if (!tempDFUPkgList.contains(oldPkg))
            {
                tempDFUPkgList.add(oldPkg);
            }

            if (!tempPkgRsrcList.contains(oldRsrc))
            {
                tempPkgRsrcList.add(oldRsrc);
            }


        }
        rsrcBean = new RscBean();

        rsrcBean.setRsrcName(rsrc);
        rsrcList.add(rsrcBean);

        if (!pkgList.contains(pkg))
        {
            pkgBean = new PkgBean();
            pkgBean.setPkgName(pkg);
            pkgBean.setUsage(pkgUsage);
            pkgBean.setAuxUsage(pkgAuxUsage);
            pkgBeanList.add(pkgBean);
        }

        if (!dfuList.contains(dfu))
        {
            dfuBean = new DfuBean();
            dfuBean.setDfuName(dfu);
            dfuBeanList.add(dfuBean);
            key = new NPODFUPrimaryKey(dfuId, dfuStartdate);
            dfuKeyList.add(key);
            dfuList.add(dfu);
        }

        if (rsrcPkgMap.get(rsrc) == null)
        {
            rsrcPkgMap.put(rsrc, tempPkgList);
            rscPkgUsageMap.put(rsrc, rscPkgUsageList);
        }
        if (rsrcDFUMap.get(rsrc) == null)
        {
            rsrcDFUMap.put(rsrc, tempRsrcDFUList);
        }
        if (PkgDFUMap.get(pkg) == null)
        {
            PkgDFUMap.put(pkg, tempPkgDFUList);
        }
        if (pkgRsrcMap.get(pkg) == null)
        {
            pkgRsrcMap.put(pkg, tempPkgRsrcList);
        }
        if (dfuPkgMap.get(dfu) == null)
        {
            dfuPkgMap.put(dfu, tempDFUPkgList);
        }
        if (pkgList.contains(pkg))
        {
            List oldRsrcList;
            oldRsrcList = pkgRsrcMap.get(oldPkg);
            if (!oldRsrcList.contains(rsrc))
            {
                oldRsrcList.add(rsrc);
            }

            List oldDFUList;
            oldDFUList = PkgDFUMap.get(oldPkg);
            if (!oldDFUList.contains(dfu))
            {
                oldDFUList.add(dfu);
            }
        }

        if (dfuList.contains(dfu))
        {
            List oldDFUList;
            oldDFUList = dfuPkgMap.get(dfu);
            if (!oldDFUList.contains(pkg))
            {
                oldDFUList.add(pkg);
            }
        }

        Trace.write(Trace.informational, "NPODFUPkgRsrcRetrievalDAO");
        npoOptimizeData.setDfuList(dfuBeanList);
        npoOptimizeData.setPkgList(pkgBeanList);
        npoOptimizeData.setRsrcList(rsrcList);
        npoOptimizeData.setPkgDFUMap(PkgDFUMap);
        npoOptimizeData.setRsrcDFUMap(rsrcDFUMap);
        npoOptimizeData.setRsrcPkgMap(rsrcPkgMap);
        npoOptimizeData.setPkgRsrcMap(pkgRsrcMap);
        npoOptimizeData.setDfuPkgMap(dfuPkgMap);
        npoOptimizeData.setRsrcPkgUsageMap(rscPkgUsageMap);
        npoOptimizeData.setDfuKeyList(dfuKeyList);

        return npoOptimizeData;
    }
	
}

/**
 * 
 * $Log: CFEDFUPkgRsrcRetrievalDAO.java,v $
 * Revision 1.1.2.1  2012/10/16 04:51:55  aanumolu
 * Added/Modified for CFE modularization as part of Analyze mode feature development
 *
 * 
 **/