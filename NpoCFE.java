package com.manu.scpoweb.npo.process.cfe;

import java.sql.Date;
import java.util.*;
import java.text.SimpleDateFormat;

import com.manu.webservices.common.ManuRuntimeException;
import com.manu.gensys.common.Pair;
import com.manu.scpoweb.common.SCPOWebException;
import com.manu.scpoweb.drm.common.PriceGlobalParam;
import com.manu.scpoweb.npo.process.optimize.beans.CplexParamBean;
import com.manu.scpoweb.npo.process.optimize.beans.DfuBean;
import com.manu.scpoweb.npo.process.optimize.beans.NPOOptimizeData;
import com.manu.scpoweb.npo.process.optimize.beans.NPORoundingRuleBean;
import com.manu.scpoweb.npo.process.optimize.beans.PkgBean;
import com.manu.scpoweb.npo.process.optimize.beans.RscBean;
import com.manu.scpoweb.npo.process.optimize.common.NPOProcessConstants;
import com.manu.scpoweb.npo.process.optimize.common.NPOException;
import com.manu.scpoweb.npo.process.optimize.common.NPOExceptionTypes;
import com.manu.scpoweb.npo.process.optimize.algs.NPOAction;
import com.manu.webservices.common.Trace;

/**
 * Created by IntelliJ IDEA.
 * User: mnalla
 * Date: Jun 27, 2008
 * Time: 12:14:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class NPOCfe
{

    NPOOptimizeData npoOptimzeData = null;
    int dur = 0;
    double[][] sortedDfuSimPrices = null;
    int[][] sortedDfuSimPricesDfuBeanIdx = null;
    List<double[]> simBookedByRscDfu = new java.util.ArrayList<double[]>();
    NPOCFEBaseControls cfeControlsObj = null;    

    NPOCfe()
    {
    }

    public NPOCfe(NPOOptimizeData npoOptimizeData, int dur, NPOCFEBaseControls cfeControlsObj)
    {
        this.npoOptimzeData = npoOptimizeData;
        this.dur = dur;
        this.cfeControlsObj = cfeControlsObj;

    }

    /**
     * call to each stage in the CFE process happens here
     */
    public void runCFE(NPOAction npoAction)  throws ManuRuntimeException
    {
        //As these parameters should get calculated for once and will be used by Cur & Rec controls,
       // these calculations are being done in NPOCFECurControls which gets called first.
        this.cfeControlsObj.calculateCommonInitialParameters(npoOptimzeData);
        initialize(npoAction);
        boolean cBookings = true;
        String defaultSortOrder = NPOProcessConstants.SORT_METHOD_PESSIMISTIC;
        // Get the NUM_PRICES_FIRST_PERIOD and NUM_PRICES_OTHER_PERIODS options
        Map priceGlobalParamMap =
                PriceGlobalParam.getInstance().getApplicationConstantsMap();
        String sortOrder = (String) priceGlobalParamMap.get(PriceGlobalParam.NPO_CFE_SORT_METHOD);
        if (sortOrder == null)
        {
            sortOrder = defaultSortOrder;
        }
        sortedDfuSimPrices = new double[dur + 1][];
        sortedDfuSimPricesDfuBeanIdx = new int[dur + 1][];
        try
        {
            for (int simDate = 0; simDate <= dur; simDate++)
            {
                if (simDate != 0 && cBookings)
                {
                    cBookings = false;
                }
                bookingSurvival(cBookings, simDate);
                sortDfuOrder(sortOrder, simDate);
                bookedAcceptedDfu(simDate);
                lastDateDfu(simDate);
            }
            bookedAcceptedRsrc(this.npoOptimzeData);

        }
        catch (Exception e)
        {
            Trace.write(Trace.error, e + "- error in runCFE() - ", e);
            throw new ManuRuntimeException(e);
        }
    }

    /**
     * simPriceByDfu and simPriceByRsc are populated at interval level in this method.
     * for dfus simPriceByDfu is populated only for menuOffsetType N(no offsetType)
     */
    private void initialize(NPOAction npoAction)  throws ManuRuntimeException
    {

        List<DfuBean> dfuBeanList = this.npoOptimzeData.getDfuList();
        List<RscBean> rscBeanList = this.npoOptimzeData.getRsrcList();
        List<PkgBean> pkgBeanList = this.npoOptimzeData.getPkgList();
        DfuBean dfuBean = null;
        RscBean rscBean = null;
        Date[] postDates = null;
        String[] rscPostDt = null;
        double[] simPriceByDfu = null;
        double[] simPriceByRsc = null;
        double[] formattedvPriceByrsc = null;
        Map<String, NPORoundingRuleBean> roundingRulesMap = this.npoOptimzeData.getRoundingRulesMap();
        Set<String> dmdTypeList = new HashSet<String>();
        NPORoundingRuleBean roundingRulesBean = null;
        // rsrcId = null, oldRsrcId = null;
        boolean formatNotSupported = false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try
        {
            for (int dfuBeanListIter = 0; dfuBeanListIter < dfuBeanList.size(); dfuBeanListIter++)
            {
                dfuBean = dfuBeanList.get(dfuBeanListIter);

                postDates = dfuBean.getPostDates();
                if (dfuBean.getDmdType() != null && dfuBean.getDmdType().trim().length() != 0)
                {
                    dmdTypeList.add(dfuBean.getDmdType());
                }
                double[] simSurvivedByDfu = new double[postDates.length];
                dfuBean.setSimSurvivedByDfu(simSurvivedByDfu);

                double[] simBkgToComeByDfu = new double[postDates.length];
                dfuBean.setSimBkgToComeByDfu(simBkgToComeByDfu);

                double[] simAcceptedByDfu = new double[postDates.length];

                double[] simBookedByDfu = new double[postDates.length];
                double[] simPriceByDfuCurPDtLog = new double[postDates.length];

                this.cfeControlsObj.setSimBkdByDfuPrice(dfuBean, simBookedByDfu);
                this.cfeControlsObj.setSimAcceptedByDfu(dfuBean, simAcceptedByDfu);
                this.cfeControlsObj.setSimPriceForLog(dfuBean, simPriceByDfuCurPDtLog);
//                StringTokenizer st = new StringTokenizer(dfuBean.getDfuName(), ":");//dfuBean.getDfuName().substring(0, dfuBean.getDfuName().indexOf(':')) ;
//                String[] dfuToken = new String[st.countTokens()];
//                int tokenCount = 0;
//                while (st.hasMoreTokens())
//                {
//                    dfuToken[tokenCount++] = st.nextToken();
//                }
//
//                java.sql.Date dfuStartDate = new java.sql.Date(sdf.parse(dfuToken[1]).getTime()); 
                if (dfuBean.getMenuOffsetType() != null)
                {                    
                    if (dfuBean.getMenuOffsetType().equals("N"))
                    {

                        simPriceByDfu = new double[postDates.length];
                        for (int dfuPstDtIter = 0; dfuPstDtIter < postDates.length; dfuPstDtIter++)
                        {
                        	try
                        	{

	                    		simPriceByDfu[dfuPstDtIter] = this.cfeControlsObj.getDfuPrice(dfuBean, 0, dfuPstDtIter, roundingRulesBean);

                        	}
                        	catch(NumberFormatException nfe)
                        	{
                                Trace.write(Trace.informational, "Invalid PriceFormat - "+roundingRulesBean.getRoundingFormat());
                		        Trace.write(Trace.informational, "supported formats are #9.99, .#9, #9.#9 only.");
                                simPriceByDfu[dfuPstDtIter] = dfuBean.getvPrice()[dfuPstDtIter];
                        	}
                        }
                        dfuBean.setSimPriceByDfu(simPriceByDfu);
                        dfuBean.setSimPriceByDfuCFE(simPriceByDfu);

                        this.cfeControlsObj.setSimPriceForLog(dfuBean, simPriceByDfu);
                    }
                }
            }
            for (int rscBeanListIter = 0; rscBeanListIter < rscBeanList.size(); rscBeanListIter++)
            {
                rscBean = rscBeanList.get(rscBeanListIter);
                rscPostDt = rscBean.getRsrcIntName();
                simPriceByRsc = new double[rscPostDt.length];
                formattedvPriceByrsc = new double[rscPostDt.length];
                
                double[] simBookedByRsrc = new double[rscPostDt.length];
                double[] simAcceptedByRsrc = new double[rscPostDt.length];
                double[] simAuxBookedByRsrc = new double[rscPostDt.length];

                double simBTCByRscPrice = 0.0;
                HashMap<String, Double> simBTCByRscPriceMap = new HashMap(); // cfe
                //String rsrcName = rscBean.getRscName();

				StringTokenizer st = new StringTokenizer(rscBean.getRscName(), ":");
				String[] rscToken = new String[st.countTokens()];
				int tokenCount = 0;
				while (st.hasMoreTokens())
				{
					rscToken[tokenCount++] = st.nextToken();
				}

				java.sql.Date rscStartDate = new java.sql.Date(sdf.parse(rscToken[1]).getTime());
                Iterator iter = dmdTypeList.iterator();
                //int i = 0;
                while (iter.hasNext())
                {
                    String dmdType = String.valueOf(iter.next());
                    simBTCByRscPriceMap.put(dmdType, simBTCByRscPrice);
                }
                this.cfeControlsObj.setSimBookedByResrcPrice(rscBean, simBookedByRsrc);
                this.cfeControlsObj.setSimAuxBookedByResrcPrice(rscBean, simAuxBookedByRsrc);
                this.cfeControlsObj.setSimAcceptedByResrcPrice(rscBean, simAcceptedByRsrc);
                this.cfeControlsObj.setSimBTCByResrcPrice(rscBean, simBTCByRscPriceMap);
                
                for (int rscPostDtIter = 0; rscPostDtIter < rscPostDt.length; rscPostDtIter++)
                {
                	
                	try
                	{
                		if(roundingRulesMap != null && !formatNotSupported)
                		{
                			roundingRulesBean = roundingRulesMap.get(rscBean.getRuleId());
                			simPriceByRsc[rscPostDtIter] = this.cfeControlsObj.getResrcPrice(rscBean, rscPostDtIter, roundingRulesBean,npoAction);
                		}
                		else
                		{
                			simPriceByRsc[rscPostDtIter] = this.cfeControlsObj.getResrcPrice(rscBean, rscPostDtIter, null,npoAction);
                		}
                	}                	
                	catch(NumberFormatException nfe)
    	        	{
                		Trace.write(Trace.informational, "Invalid PriceFormat - "+roundingRulesBean.getRoundingFormat());
                		Trace.write(Trace.informational, "supported formats are #9.99, .#9, #9.#9 only.");
                        if(npoAction!= null){
                            NPOException.create("Rsc", rscToken[0],rscStartDate, null, NPOExceptionTypes.NPO_ROUNDING_RULES_ERROR, NPOExceptionTypes.FUNC_CFE_INITIALIZE,npoAction);
                        }
                        simPriceByRsc[rscPostDtIter] = rscBean.getVPriceByRsrcInt()[rscPostDtIter];
                        formatNotSupported = true;
                    }
                    formattedvPriceByrsc[rscPostDtIter] = simPriceByRsc[rscPostDtIter];
                }
                
                rscBean.setSimPriceByRsc(simPriceByRsc);
                rscBean.setFormattedvPriceByRsrcInt(formattedvPriceByrsc);
            }
        }
        
        catch (Exception e)
        {
            Trace.write(Trace.error,  " - error in initialize() - ",e);
            throw new ManuRuntimeException(e);
        }
        
        updateSimprices(dfuBeanList, rscBeanList, pkgBeanList, roundingRulesMap,npoAction);
        
    }

    /**
     * @param dfuBeanList list of DfuBean objects
     * @param rscBeanList list of RscBean objects
     * @param pkgBeanList list of PkgBean objects
     */
    public void updateSimprices(List<DfuBean> dfuBeanList, List<RscBean> rscBeanList, List<PkgBean> pkgBeanList, Map<String, NPORoundingRuleBean> roundingRulesMap,NPOAction npoAction) throws ManuRuntimeException
    {
        DfuBean dfuBean = null;
        PkgBean pkgBean = null;
        RscBean rscBean = null;
        Date[] dfuPostDt = null;
        double[] evalPrice = null;
        int[][][] dfuIntRscIndex = null;
        int[][][] dfuIntRscInt = null;
        int[] rscIdx = null;
        int[] dfuIntrvlrsc = null;
        double[] rscSimPrice = null;
        int[] dfuPkgIdx = null;
        double[] simPriceByDfuPstDt = null;
        double[] simPriceBySDfuPstDtCFE = null;
        double[] simPriceByDfuPDtLog = null;
        NPORoundingRuleBean roundingRuleBean = null;
        //String rsrcId = null ;//, oldRsrcId = null;
        boolean formatNotSupported = false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try
        {
            for (int dfuBeanListIter = 0; dfuBeanListIter < dfuBeanList.size(); dfuBeanListIter++)
            {
                dfuBean = dfuBeanList.get(dfuBeanListIter);
                //String dfuNameArr[] = dfuBean.getDfuName().split("_");
            	//rsrcId = dfuNameArr[0];
            	StringTokenizer st = new StringTokenizer(dfuBean.getDfuName(), ":");
                String[] dfuToken = new String[st.countTokens()];
                int tokenCount = 0;
                while (st.hasMoreTokens())
                {
                    dfuToken[tokenCount++] = st.nextToken();
                }

                java.sql.Date dfuStartDate = new java.sql.Date(sdf.parse(dfuToken[1]).getTime());
                dfuPostDt = dfuBean.getPostDates();
                simPriceByDfuPstDt = new double[dfuPostDt.length];
                simPriceBySDfuPstDtCFE = new double[dfuPostDt.length];
                simPriceByDfuPDtLog = new double[dfuPostDt.length];
                for (int dfuPostDtIter = 0; dfuPostDtIter < dfuPostDt.length; dfuPostDtIter++)
                {
                    //getPkgIndex would give the index of pkgObjects associated with dfu
                    evalPrice = new double[dfuBean.getPkgIndex().length];
                    dfuPkgIdx = dfuBean.getPkgIndex();
                    
                    //iterate through the packages associated with dfu
                    for (int dfuPkgIdxIter = 0; dfuPkgIdxIter < dfuPkgIdx.length; dfuPkgIdxIter++)
                    {
                        pkgBean = pkgBeanList.get(dfuPkgIdx[dfuPkgIdxIter]);
                        evalPrice[dfuPkgIdxIter] = 0;
                        dfuIntRscIndex = pkgBean.getDfuRsrcIndex();
                        dfuIntRscInt = pkgBean.getDfuRsrcInt();
                        rscIdx = pkgBean.getRsrcIndex();
                        //index of dfuBean(represented by dfuBeanListIter) in the dfuList associated with package 
                        int dfuindex = Arrays.binarySearch(pkgBean.getDfuPkg(), dfuBeanListIter);
                        
                        //dfuIntrvlrsc gives the resources associated with a given dfuindex and dfuPostdate
                        dfuIntrvlrsc = dfuIntRscIndex[dfuindex][dfuPostDtIter];
                        for (int dfuIntrvlrscIter = 0; dfuIntrvlrscIter < dfuIntrvlrsc.length && dfuIntrvlrscIter < rscIdx.length; dfuIntrvlrscIter++)
                        {
                            rscBean = rscBeanList.get(rscIdx[dfuIntrvlrscIter]);
                            rscSimPrice = rscBean.getSimPriceByRsc();
                            //dfuIntRscInt gives the matching interval (RSRC and DFU) index
                            evalPrice[dfuPkgIdxIter] = evalPrice[dfuPkgIdxIter] + rscSimPrice[dfuIntRscInt[dfuindex][dfuPostDtIter][dfuIntrvlrscIter]];
                        }

                        //A - additive and M - multiplicative
                        if (dfuBean.getMenuOffsetType().equals("A"))
                        {
                            //evalPrice[dfuPkgIdxIter] = evalPrice[dfuPkgIdxIter] + (dfuIntRscIndex.length*dfuBean.getMenuOffsetValue());
                            evalPrice[dfuPkgIdxIter] = evalPrice[dfuPkgIdxIter] + (dfuIntrvlrsc.length * dfuBean.getMenuOffsetValue());
                        }
                        else if (dfuBean.getMenuOffsetType().equals("M"))
                        {
                            evalPrice[dfuPkgIdxIter] = evalPrice[dfuPkgIdxIter] * dfuBean.getMenuOffsetValue();
                        }
                    }
                    double maxEvalPrice = getMaxEvalPrice(evalPrice);
                    simPriceBySDfuPstDtCFE[dfuPostDtIter] = maxEvalPrice;

                    try
                    {
                    	if(roundingRulesMap != null && !formatNotSupported)
                    	{
                    		roundingRuleBean = roundingRulesMap.get(dfuBean.getRuleId());
                    		simPriceByDfuPstDt[dfuPostDtIter] = this.cfeControlsObj.getDfuPrice(dfuBean, maxEvalPrice, dfuPostDtIter, roundingRuleBean);
                    	}
                    	else
                    	{
                    		simPriceByDfuPstDt[dfuPostDtIter] = this.cfeControlsObj.getDfuPrice(dfuBean, maxEvalPrice, dfuPostDtIter, null);
                    	}
                    }
                    catch(NumberFormatException nfe)
                    {
                        if(npoAction != null){
                            NPOException.create("DFU", dfuToken[0],dfuStartDate, null, NPOExceptionTypes.NPO_ROUNDING_RULES_ERROR, NPOExceptionTypes.FUNC_CFE_UPDATE_SIM_PRICES,npoAction);
                        }

                		simPriceByDfuPstDt[dfuPostDtIter] = maxEvalPrice;
                        formatNotSupported = true;

                    }
                    simPriceByDfuPDtLog[dfuPostDtIter] = maxEvalPrice;
                }
           
                dfuBean.setSimPriceByDfu(simPriceByDfuPstDt);
                dfuBean.setSimPriceByDfuCFE(simPriceBySDfuPstDtCFE);
                this.cfeControlsObj.setSimPriceForLog(dfuBean, simPriceByDfuPDtLog);
            }
        }
        catch (Exception e)
        {
            Trace.write(Trace.error, e + " - error in updateSimprices() - ", e);
            throw new ManuRuntimeException(e);
        }

    }

    /**
     * @param evalPrice
     * @return maxPrice
     */
    private double getMaxEvalPrice(double[] evalPrice)  throws ManuRuntimeException
    {
        double maxEvalPrice = 0;
        try
        {
            for (int evalPrIter = 0; evalPrIter < evalPrice.length; evalPrIter++)
            {
                if (evalPrice[evalPrIter] > maxEvalPrice)
                {
                    maxEvalPrice = evalPrice[evalPrIter];
                }
            }
        }
        catch (Exception e)
        {
            Trace.write(Trace.error, " - error in getMaxEvalPrice() - ", e);
            throw new ManuRuntimeException(e);
        }
        return maxEvalPrice;
    }

    /**
     * @param currentBookings
     * @param simDate
     */
    private void bookingSurvival(boolean currentBookings, int simDate)   throws ManuRuntimeException
    {

        List<DfuBean> dfuBeanList = this.npoOptimzeData.getDfuList();
        List<RscBean> rscBeanList = this.npoOptimzeData.getRsrcList();
        List<PkgBean> pkgBeanList = this.npoOptimzeData.getPkgList();

        DfuBean dfuBean = null;
        RscBean rscBean = null;
        PkgBean pkgBean = null;
        int packageUsage = 0;
        float packageAuxUsage = 0;
        double[] simPriceByDfu = null;

        try
        {
            for (int dfuBeanListIter = 0; dfuBeanListIter < dfuBeanList.size(); dfuBeanListIter++)
            {
                dfuBean = dfuBeanList.get(dfuBeanListIter);

                double[] simSurvivedByDfu = dfuBean.getSimSurvivedByDfu();
                double[] simBkgToComeByDfu = dfuBean.getSimBkgToComeByDfu();
                double[] simBkdByDfuPrice = this.cfeControlsObj.getSimBkdByDfuPrice(dfuBean);

                double[] surRate = dfuBean.getSurRate();
                double[] baseDemand = dfuBean.getBaseDemand();

                double[] ownPriceElasticityAtRefprice = dfuBean.getoPer();
                simPriceByDfu = dfuBean.getSimPriceByDfu();
                double[] refPrice = dfuBean.getRefPrice();
                

                if (simDate >= simSurvivedByDfu.length)
                {
                    continue;
                }

                //for 1st day in the horizon we consider current bookings, from next day we will have 1st day calculated value so
                //current bookings is not required from 2nd day
                if (currentBookings)
                {
                    simSurvivedByDfu[simDate] = dfuBean.getcBookings() * surRate[simDate];
                } 
                else
                {
                    simSurvivedByDfu[simDate] = simBkdByDfuPrice[simDate - 1] * surRate[simDate];
                }
                dfuBean.setSimSurvivedByDfu(simSurvivedByDfu);

                if (dfuBean.isFixedPriceSW())
                {
                    simBkgToComeByDfu[simDate] = Math.max(0, baseDemand[simDate]);
                }
                 else
                {
                    try
                    {
                    	simBkgToComeByDfu[simDate] = Math.max(0, baseDemand[simDate] * (1 + (ownPriceElasticityAtRefprice[simDate] * ((simPriceByDfu[simDate] - refPrice[simDate]) / refPrice[simDate]))));
                    }
                    catch(Exception e)
                    {
                    	Trace.write(Trace.error, "refPrice or baseDemand missing for some of the dfu's or for some intervals of dfu's");
                        throw new ManuRuntimeException(e);

                    }
                } 
                dfuBean.setSimBkgToComeByDfu(simBkgToComeByDfu);
            }

            for (int rscBeanListIter = 0; rscBeanListIter < rscBeanList.size(); rscBeanListIter++)
            {
                double[] simBookedByRsc = null, simAuxBookedByRsc = null;
                rscBean = rscBeanList.get(rscBeanListIter);

                simBookedByRsc = this.cfeControlsObj.getSimBookedByResrcPrice(rscBean);
                simAuxBookedByRsc = this.cfeControlsObj.getSimAuxBookedByResrcPrice(rscBean);

                int[] rscPkg = rscBean.getRscPkg();

                if (simDate >= simBookedByRsc.length)
                {
                    continue;
                }

                for (int pkgIndex = 0; pkgIndex < rscPkg.length; pkgIndex++)
                {
                    pkgBean = pkgBeanList.get(rscPkg[pkgIndex]);
                    int[] dfuPkg = pkgBean.getDfuPkg();
                    packageUsage = pkgBean.getUsage();
                    packageAuxUsage = pkgBean.getAuxUsage();
                    for (int dfuIndex = 0; dfuIndex < dfuPkg.length; dfuIndex++)
                    {
                        dfuBean = dfuBeanList.get(dfuPkg[dfuIndex]);
                        double[] simSurvivedByDfu = dfuBean.getSimSurvivedByDfu();

                        if (simDate >= simSurvivedByDfu.length)
                        {
                            continue;
                        }
                        simBookedByRsc[simDate] += simSurvivedByDfu[simDate] * packageUsage;
                        simAuxBookedByRsc[simDate] += simSurvivedByDfu[simDate] * packageAuxUsage;
                    }
                }
                this.cfeControlsObj.setSimBookedByResrcPrice(rscBean, simBookedByRsc);
                this.cfeControlsObj.setSimAuxBookedByResrcPrice(rscBean, simAuxBookedByRsc);
            }
        }
        catch (Exception e)
        {
            Trace.write(Trace.error, "Sufficient data is not there for running CFE Analysis");
            Trace.write(Trace.error, " - error in  bookingSurvival() - ", e);
            throw new ManuRuntimeException(e);
        }
    }

    private double getMaxVPriceOfDfuPkgs(int[] dfuPkgIdx)
    {
        List<PkgBean> pkgBeans = this.npoOptimzeData.getPkgList();
        double maxVPriceByPkg = 0;
        for (int i = 0; i < dfuPkgIdx.length; i++)
        {
            if (i == 0)
            {
                maxVPriceByPkg = pkgBeans.get(dfuPkgIdx[0]).getVPriceByPkg();
            } else
            {
                if (maxVPriceByPkg <= pkgBeans.get(dfuPkgIdx[i]).getVPriceByPkg())
                {
                    maxVPriceByPkg = pkgBeans.get(dfuPkgIdx[i]).getVPriceByPkg();
                }
            }
        }
        return maxVPriceByPkg;
    }


    /**
     * @param sortType
     */
    public void sortDfuOrder(String sortType, int simDate)  throws ManuRuntimeException
    {
        List<DfuBean> dfuBeanList = this.npoOptimzeData.getDfuList();
        List<Pair> dfuBeanSimPriceIdx = new ArrayList();
        Pair dfuBeanSimPriceIdxPair = null;
        DfuBean dfuBean = null;
        double[] simPrices = null;
        double simPriceForSimDate;
        Double [] simPriceArr = null;
        //int simPriceArrIdx = 0;
        //int simPriceArrLen = 0;
        try
        {
        	List<Double> temp =new ArrayList();
//            for (int dfuBeanListIter = 0; dfuBeanListIter < dfuBeanList.size(); dfuBeanListIter++)
//            {
//                dfuBean = dfuBeanList.get(dfuBeanListIter);//get a bean 
//                simPrices = dfuBean.getSimPriceByDfu();// 
//                if (simPrices.length > simDate)
//                {
//                    simPriceForSimDate = simPrices[simDate];
//                    if (simPriceForSimDate >= 0)
//                    {
//                        simPriceArrLen++;
//                    }
//                }
//            }
//            simPriceArr = new Double[simPriceArrLen];
//            for (int dfuBeanListIter = 0; dfuBeanListIter < dfuBeanList.size(); dfuBeanListIter++)
//            {
//                dfuBean = dfuBeanList.get(dfuBeanListIter);
//                simPrices = dfuBean.getSimPriceByDfu();
//                if (simPrices.length > simDate)
//                {
//                    simPriceForSimDate = simPrices[simDate];
//                    if (simPriceForSimDate >= 0)
//                    {
//                        if (sortType.equalsIgnoreCase(NPOProcessConstants.SORT_METHOD_PESSIMISTIC))
//                        {
//                            if (dfuBean.getFxPriceMult() == 0)
//                            {
//                                dfuBean.setFxPriceMult(0.001); //This is to eliminate divide by zero error.   
//                            }
//                            simPriceForSimDate = simPriceForSimDate / dfuBean.getFxPriceMult();
//                        } else if (sortType.equalsIgnoreCase(NPOProcessConstants.SORT_METHOD_OPTIMISTIC))
//                        {
//                            simPriceForSimDate = simPriceForSimDate * dfuBean.getFxPriceMult();
//                        }
//                        simPriceArr[simPriceArrIdx] = simPriceForSimDate;
//                        dfuBeanSimPriceIdxPair = new Pair(dfuBeanListIter, simPriceForSimDate);
//                        dfuBeanSimPriceIdx.add(dfuBeanSimPriceIdxPair);
//                        simPriceArrIdx++;
//                    }
//                }
//            }
            //start///
            for (int dfuBeanListIter = 0; dfuBeanListIter < dfuBeanList.size(); dfuBeanListIter++)
            {
                dfuBean = dfuBeanList.get(dfuBeanListIter); 
                simPrices = dfuBean.getSimPriceByDfu();
                if (simPrices.length > simDate)
                {
                    simPriceForSimDate = simPrices[simDate];
                    if (simPriceForSimDate >= 0)
                    {
                        
                        if (sortType.equalsIgnoreCase(NPOProcessConstants.SORT_METHOD_PESSIMISTIC))
                        {
                            if (dfuBean.getFxPriceMult() == 0)
                            {
                                dfuBean.setFxPriceMult(0.001); //This is to eliminate devide by zero error.   
                            }
                            simPriceForSimDate = simPriceForSimDate / dfuBean.getFxPriceMult();
                        } else if (sortType.equalsIgnoreCase(NPOProcessConstants.SORT_METHOD_OPTIMISTIC))
                        {
                            simPriceForSimDate = simPriceForSimDate * dfuBean.getFxPriceMult();
                        }
                        temp.add(simPriceForSimDate);
                        dfuBeanSimPriceIdxPair = new Pair(dfuBeanListIter, simPriceForSimDate);
                        dfuBeanSimPriceIdx.add(dfuBeanSimPriceIdxPair);
                    }
                }
            }
            simPriceArr= (Double[]) temp.toArray();
            ///end
            
            sortDfuSimPrices(dfuBeanSimPriceIdx, sortType);
            sortedDfuSimPrices[simDate] = new double[simPriceArr.length];
            sortedDfuSimPricesDfuBeanIdx[simDate] = new int[simPriceArr.length];
            Arrays.sort(simPriceArr);
            for (int sortedSimPriceArrIter = 0; sortedSimPriceArrIter < simPriceArr.length; sortedSimPriceArrIter++)
            {
                sortedDfuSimPrices[simDate][sortedSimPriceArrIter] = simPriceArr[sortedSimPriceArrIter];
                sortedDfuSimPricesDfuBeanIdx[simDate][sortedSimPriceArrIter] = (Integer) dfuBeanSimPriceIdx.get(sortedSimPriceArrIter).getLeft();
            }
        }
        catch (Exception e)
        {
            Trace.write(Trace.error, " error in  sortDfuOrder() - ", e);
            throw new ManuRuntimeException(e);
        }


    }

    /**
     * @param dfuBeansimPricesLst
     * @param sortType
     */
    public void sortDfuSimPrices(List<Pair> dfuBeansimPricesLst, String sortType)  throws ManuRuntimeException
    {
        Pair dfuBeansimPrice = null;
        Pair minDfuBeansimPrice = null;
        Pair maxDfuBeansimPrice = null;
        int dfuBeansimPricesLstIter = 0;
        double minSimPrice = 0;
        double maxSimPrice = 0;
        double dfuSimPrice = 0;
        int minPriceIdx = 0;
        int maxPriceIdx = 0;
        int dfuBeansimPricesLstInnerIter = 0;
        try
        {
            for (dfuBeansimPricesLstIter = 0; dfuBeansimPricesLstIter < dfuBeansimPricesLst.size(); dfuBeansimPricesLstIter++)
            {
                if (sortType.equalsIgnoreCase(NPOProcessConstants.SORT_METHOD_PESSIMISTIC))
                {
                    for (dfuBeansimPricesLstInnerIter = 0 + dfuBeansimPricesLstIter; dfuBeansimPricesLstInnerIter < dfuBeansimPricesLst.size(); dfuBeansimPricesLstInnerIter++)
                    {
                        dfuBeansimPrice = dfuBeansimPricesLst.get(dfuBeansimPricesLstInnerIter);
                        dfuSimPrice = (Double) dfuBeansimPrice.getRight();
                        if (dfuBeansimPricesLstInnerIter == (0 + dfuBeansimPricesLstIter))
                        {
                            minSimPrice = dfuSimPrice;
                            minPriceIdx = dfuBeansimPricesLstInnerIter;
                            minDfuBeansimPrice = dfuBeansimPrice;
                        }
                        if (dfuSimPrice < minSimPrice)
                        {
                            minSimPrice = dfuSimPrice;
                            minPriceIdx = dfuBeansimPricesLstInnerIter;
                            minDfuBeansimPrice = dfuBeansimPrice;
                        }
                    }
                    dfuBeansimPricesLst.remove(minPriceIdx);
                    dfuBeansimPricesLst.add(dfuBeansimPricesLstIter, minDfuBeansimPrice);
                } else if (sortType.equalsIgnoreCase(NPOProcessConstants.SORT_METHOD_OPTIMISTIC))
                {
                    for (dfuBeansimPricesLstIter = 0; dfuBeansimPricesLstIter < dfuBeansimPricesLst.size(); dfuBeansimPricesLstIter++)
                    {
                        for (dfuBeansimPricesLstInnerIter = 0 + dfuBeansimPricesLstIter; dfuBeansimPricesLstInnerIter < dfuBeansimPricesLst.size(); dfuBeansimPricesLstInnerIter++)
                        {
                            dfuBeansimPrice = dfuBeansimPricesLst.get(dfuBeansimPricesLstInnerIter);
                            dfuSimPrice = (Double) dfuBeansimPrice.getRight();
                            if (dfuBeansimPricesLstInnerIter == (0 + dfuBeansimPricesLstIter))
                            {
                                maxSimPrice = dfuSimPrice;
                                maxPriceIdx = dfuBeansimPricesLstInnerIter;
                                maxDfuBeansimPrice = dfuBeansimPrice;
                            }
                            if (dfuSimPrice > maxSimPrice)
                            {
                                maxSimPrice = dfuSimPrice;
                                maxPriceIdx = dfuBeansimPricesLstInnerIter;
                                maxDfuBeansimPrice = dfuBeansimPrice;
                            }
                        }
                        dfuBeansimPricesLst.remove(maxPriceIdx);
                        dfuBeansimPricesLst.add(dfuBeansimPricesLstIter, maxDfuBeansimPrice);
                    }
                }
            }
        }
        catch (Exception e)
        {
            Trace.write(Trace.error, " - Error in sortDfuSimPrices() - ", e);
            throw new ManuRuntimeException(e);
        }
    }

    /**
     * @param simDate
     */
    public void bookedAcceptedDfu(int simDate) throws ManuRuntimeException
    {
        List<DfuBean> dfuBeanList = this.npoOptimzeData.getDfuList();
        List<RscBean> rscBeanList = this.npoOptimzeData.getRsrcList();
        List<PkgBean> pkgBeanList = this.npoOptimzeData.getPkgList();
        int[][] sortedDfuIdxs = this.sortedDfuSimPricesDfuBeanIdx;
        int dfuBeanIdx = 0;
        double[] simBookedByRsrc = null;
        double[] simAuxBookedByRsrc = null;
        double[] simBookedByDfu = null;
        double[] simAcceptedByDfu = null;
        double[] simAcceptedByRsrc = null;
        HashMap<String, Double> simBTCByRscpriceMap = new HashMap<String, Double>();
        double simBTCByRscprice = 0.0;
        int packageUsage = 0;
        float packageAuxUsage = 0;

        // This has to be changed later, should be based on the dfu sorted list for the simDate
        try
        {
            for (int sortedDfuIdxsIter = 0; sortedDfuIdxsIter < sortedDfuIdxs[simDate].length; sortedDfuIdxsIter++)
            {
                dfuBeanIdx = sortedDfuIdxs[simDate][sortedDfuIdxsIter];
                //DfuBean dfuBean = dfuBeanList.get(dfuBeanListItr);
                DfuBean dfuBean = dfuBeanList.get(dfuBeanIdx);
                int[] pkgIndex = dfuBean.getPkgIndex();
                List<Double> minLeftOverList = new java.util.ArrayList<Double>();

                for (int pkgBeanListItr = 0; pkgBeanListItr < pkgIndex.length; pkgBeanListItr++)
                {
                    PkgBean pkgBean = pkgBeanList.get(pkgIndex[pkgBeanListItr]);
                    int[] dfuPkg = pkgBean.getDfuPkg();
                    int[][][] dfuIntRscIndex = pkgBean.getDfuRsrcIndex();
                    packageUsage = pkgBean.getUsage();
                    packageAuxUsage = pkgBean.getAuxUsage();
                    int dfuIndex = Arrays.binarySearch(dfuPkg, dfuBeanIdx);

                    for (int rscBeanItr = 0; simDate < dfuIntRscIndex[dfuIndex].length &&
                            rscBeanItr < dfuIntRscIndex[dfuIndex][simDate].length; rscBeanItr++)
                    {
                        RscBean rscBean = rscBeanList.get(dfuIntRscIndex[dfuIndex][simDate][rscBeanItr]);

                        double[] rscCapacity = null;
                        rscCapacity = this.cfeControlsObj.getResrcCapacity(rscBean);
                        simBookedByRsrc = this.cfeControlsObj.getSimBookedByResrcPrice(rscBean);
                        minLeftOverList.add((rscCapacity[simDate] - simBookedByRsrc[simDate]) / packageUsage);
                    }
                }
                double minLeftOver = minLeftOverList.get(0);
                for (int minLeftOverItr = 1; minLeftOverItr < minLeftOverList.size(); minLeftOverItr++)
                {
                    if (minLeftOverList.get(minLeftOverItr) < minLeftOver)
                    {
                        minLeftOver = minLeftOverList.get(minLeftOverItr);
                    }
                }

                double minBooking;
                double[] simBkgToComeByDfu = dfuBean.getSimBkgToComeByDfu();
                //capping the demand with available capacity.
                minBooking = Math.min(minLeftOver, simBkgToComeByDfu[simDate]);
                simBookedByDfu = this.cfeControlsObj.getSimBkdByDfuPrice(dfuBean);
                simAcceptedByDfu = this.cfeControlsObj.getSimAcceptedByDfu(dfuBean);

                simAcceptedByDfu[simDate] = Math.max(0, minBooking);
                double[] simSurvivedByDfu = dfuBean.getSimSurvivedByDfu();

                simBookedByDfu[simDate] = simAcceptedByDfu[simDate] + simSurvivedByDfu[simDate];
                this.cfeControlsObj.setSimBkdByDfuPrice(dfuBean, simBookedByDfu);
                this.cfeControlsObj.setSimAcceptedByDfu(dfuBean, simAcceptedByDfu);

                for (int pkgBeanListItr = 0; pkgBeanListItr < pkgIndex.length; pkgBeanListItr++)
                {
                    PkgBean pkgBean = pkgBeanList.get(pkgIndex[pkgBeanListItr]);
                    int[][][] dfuIntRscInt = pkgBean.getDfuRsrcInt();
                    int[][][] dfuIntRscIndex = pkgBean.getDfuRsrcIndex();
                    int[] dfuPkg = pkgBean.getDfuPkg();
                    packageUsage = pkgBean.getUsage();
                    int dfuIndex = Arrays.binarySearch(dfuPkg, dfuBeanIdx);


                    for (int rscBeanItr = 0; simDate < dfuIntRscIndex[dfuIndex].length &&
                            rscBeanItr < dfuIntRscInt[dfuIndex][simDate].length; rscBeanItr++)
                    {
                        //RscBean rscBean = rscBeanList.get(dfuIntRscInt[dfuIndex][simDate][rscBeanItr]);
                        RscBean rscBean = rscBeanList.get(dfuIntRscIndex[dfuIndex][simDate][rscBeanItr]);
                        simBookedByRsrc = this.cfeControlsObj.getSimBookedByResrcPrice(rscBean);
                        simAuxBookedByRsrc = this.cfeControlsObj.getSimAuxBookedByResrcPrice(rscBean);
                        simAcceptedByRsrc = this.cfeControlsObj.getSimAcceptedByResrcPrice(rscBean);
                        simBTCByRscpriceMap = this.cfeControlsObj.getSimBTCByResrcPrice(rscBean);
                        int simBookedByRsrcInt = dfuIntRscInt[dfuIndex][simDate][rscBeanItr];
                        // simBookedbyResrc for Visitor and Member
                        if (dfuBean.getDmdType() != null && dfuBean.getDmdType().trim().length() != 0)
                        {
                            simBTCByRscprice = simBTCByRscpriceMap.get(dfuBean.getDmdType());
                            simBTCByRscprice += simBkgToComeByDfu[simDate];
                            String demandType = dfuBean.getDmdType();
                            simBTCByRscpriceMap.put(demandType, simBTCByRscprice);
                        }
                        if(simBookedByRsrcInt == -1){
                           // double[]  dmdAtDfuPrice = this.cfeControlsObj.getSimAcceptedByDfu(dfuBean);
                            simBookedByRsrc[simBookedByRsrcInt] += simAcceptedByDfu[simDate] * packageUsage;
                        }
                        simBookedByRsrc[simBookedByRsrcInt] += simAcceptedByDfu[simDate] * packageUsage;
                        simAuxBookedByRsrc[simBookedByRsrcInt] += simAcceptedByDfu[simDate] * packageAuxUsage;
                        simAcceptedByRsrc[simBookedByRsrcInt] += simAcceptedByDfu[simDate] * packageUsage;
                        this.cfeControlsObj.setSimAcceptedByResrcPrice(rscBean, simAcceptedByRsrc);
                        this.cfeControlsObj.setSimBTCByResrcPrice(rscBean, simBTCByRscpriceMap);
                        this.cfeControlsObj.setSimBookedByResrcPrice(rscBean, simBookedByRsrc);

                    }
                }

                int dfulen = 0;
                int lastButOnePostdateIdx = 0;
                double[] survivalRate = null;
                double sumDmdAtDfuDtPrice = 0;
                dfulen = dfuBean.getPostDates().length;

                double[] dmdAtDfuPrice = this.cfeControlsObj.getSimAcceptedByDfu(dfuBean);
                survivalRate = dfuBean.getSurRate();
                lastButOnePostdateIdx = dfulen - 2;
                if (lastButOnePostdateIdx < 0)
                {
                    lastButOnePostdateIdx = dfulen - 1;
                }
                //Calculates and sets SimCurBkdUnconstraints to dfuBean.
                this.cfeControlsObj.calculateSimBkdUncons(dfuBean, dfulen);

                sumDmdAtDfuDtPrice = 0;
                double sumDmdAtDfuDtPriceSurv = 0;
                for (int intrvl = 0; intrvl < dfulen; intrvl++)
                {
                    double finalSurRate = 1;
                    for (int postDateCountForSurRate = (intrvl + 1); postDateCountForSurRate < dfulen; postDateCountForSurRate++)
                    {
                        finalSurRate = finalSurRate * survivalRate[postDateCountForSurRate];
                    }
                    sumDmdAtDfuDtPrice = sumDmdAtDfuDtPrice + dmdAtDfuPrice[intrvl];
                    sumDmdAtDfuDtPriceSurv = sumDmdAtDfuDtPriceSurv + dmdAtDfuPrice[intrvl] * finalSurRate;
                }
                this.cfeControlsObj.setDmdSurvAtDfuPrice(dfuBean, sumDmdAtDfuDtPriceSurv);

                //calculates & sets No-shows & cancellations at rec controls
                this.cfeControlsObj.setDfuMiscParameters(dfuBean, sumDmdAtDfuDtPrice);
            }
        }
        catch (Exception e)
        {
            Trace.write(Trace.error, " - error in bookedAcceptedDfu() - ", e);
            throw new ManuRuntimeException(e);
        }
    }

    /**
     * @param simDate
     */
    public void lastDateDfu(int simDate) throws  ManuRuntimeException
    {

        List<DfuBean> dfuBeanList = this.npoOptimzeData.getDfuList();
        List<RscBean> rscBeanList = this.npoOptimzeData.getRsrcList();
        List<PkgBean> pkgBeanList = this.npoOptimzeData.getPkgList();
        DfuBean dfuBean = null;
        int simDateLen = 0;
        int[] pkgIndex = null;
        String[] rscIntName = null;
        double[] simBookedByRsrc = null;
        double[] simAuxBookedByRsrc = null;
        int packageUsage = 0;
        float packageAuxUsage = 0;

        try
        {
            for (int dfuBeanListIter = 0; dfuBeanListIter < dfuBeanList.size(); dfuBeanListIter++)
            {
                dfuBean = dfuBeanList.get(dfuBeanListIter);
                simDateLen = dfuBean.getSimPriceByDfu().length - 1;
                if (simDate == simDateLen)
                {
                    pkgIndex = dfuBean.getPkgIndex();

                    for (int pkgBeanListItr = 0; pkgBeanListItr < pkgIndex.length; pkgBeanListItr++)
                    {
                        PkgBean pkgBean = pkgBeanList.get(pkgIndex[pkgBeanListItr]);
                        int[] dfuPkg = pkgBean.getDfuPkg();
                        int[][][] dfuIntRscIndex = pkgBean.getDfuRsrcIndex();
                        int dfuIndex = Arrays.binarySearch(dfuPkg, dfuBeanListIter);
                        packageUsage = pkgBean.getUsage();

                        for (int rscBeanItr = 0; simDate < dfuIntRscIndex[dfuIndex].length &&
                                rscBeanItr < dfuIntRscIndex[dfuIndex][simDate].length; rscBeanItr++)
                        {
                            RscBean rscBean = rscBeanList.get(dfuIntRscIndex[dfuIndex][simDate][rscBeanItr]);
                            rscIntName = rscBean.getRscIntName();
                            simBookedByRsrc = this.cfeControlsObj.getSimBookedByResrcPrice(rscBean);
                            simAuxBookedByRsrc = this.cfeControlsObj.getSimAuxBookedByResrcPrice(rscBean);

                            double[] simBkdDFUPrice = this.cfeControlsObj.getSimBkdByDfuPrice(dfuBean);
                            for (int rscIntNameIter = 0; rscIntNameIter < rscIntName.length; rscIntNameIter++)
                            {
                                if (rscIntNameIter > simDate)
                                {
                                    simBookedByRsrc[rscIntNameIter] = simBookedByRsrc[rscIntNameIter] + simBkdDFUPrice[simDate] * packageUsage;
                                    simAuxBookedByRsrc[rscIntNameIter] = simAuxBookedByRsrc[rscIntNameIter] + simBkdDFUPrice[simDate] * packageAuxUsage;
                                }
                            }
                            this.cfeControlsObj.setSimBookedByResrcPrice(rscBean, simBookedByRsrc);
                            this.cfeControlsObj.setSimAuxBookedByResrcPrice(rscBean, simAuxBookedByRsrc);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            Trace.write(Trace.error, " - error in lastDateDfu() - ", e);
            throw new ManuRuntimeException(e);
        }
    }

    /**
     * @param npoOptimzeData
     * @throws ManuRuntimeException
     */
    public void bookedAcceptedRsrc(NPOOptimizeData npoOptimzeData) throws ManuRuntimeException
    {
        List<DfuBean> dfuBeanList = npoOptimzeData.getDfuList();
        List<RscBean> rscBeanList = npoOptimzeData.getRsrcList();
        List<PkgBean> pkgBeanList = npoOptimzeData.getPkgList();

        RscBean rsrcBean = null;
        PkgBean pkgBean = null;
        DfuBean dfuBean = null;
        int[] rsrcPkg = null;
        int[] pkgDfus = null;
        double[] simAcceptedByRsrc = null;
        double[] simAcceptedByDfu = null;
        double[][][] simRevenueByRsrcDfu = null;
        double dfuPricePerRscDate = 0;
        int rsrcInts = 0;
        try
        {
            int[][][] rsrcPkgDfuIndtr = null;
            int[][][] rsrcPkgDfuIntrvl = null;
            double[] simBkdCurUnconsByR = null;
            int packageUsage = 0;
            int lastintrvl = 0;
            for (int rsrcBeanIter = 0; rsrcBeanIter < rscBeanList.size(); rsrcBeanIter++)
            {
                rsrcBean = rscBeanList.get(rsrcBeanIter);
                rsrcInts = rsrcBean.getRsrcIntName().length;
                rsrcPkgDfuIndtr = rsrcBean.getRscPkgDfuIndicator();
                rsrcPkgDfuIntrvl = rsrcBean.getRscPkgDfuInterval();
                simBkdCurUnconsByR = new double[rsrcInts];
                for (int rsrcIntsIter = 0; rsrcIntsIter < rsrcInts; rsrcIntsIter++)
                {
                    rsrcPkg = rsrcBean.getRscPkg();
                    for (int rsrcPkgIter = 0; rsrcPkgIter < rsrcPkg.length; rsrcPkgIter++)
                    {
                        pkgBean = pkgBeanList.get(rsrcPkg[rsrcPkgIter]);
                        pkgDfus = pkgBean.getDfuPkg();
                        packageUsage = pkgBean.getUsage();
                        //simBkdCurUnconsByR[rsrcIntsIter] = new double[pkgDfus.length];
                        for (int pkgDfusIter = 0; pkgDfusIter < pkgDfus.length; pkgDfusIter++)
                        {
                            dfuBean = dfuBeanList.get(pkgDfus[pkgDfusIter]);
                            lastintrvl = dfuBean.getPostDates().length - 1;
                            int rsrcPkgDfuIndicator = rsrcPkgDfuIndtr[rsrcIntsIter][rsrcPkgIter][pkgDfusIter];
                            int rsrcPkgIntrvl = rsrcPkgDfuIntrvl[rsrcIntsIter][rsrcPkgIter][pkgDfusIter];
                            if(dfuBean.getSimBookedByDfuCurPriceUncons() != null)
                            {
                            	if (rsrcPkgDfuIndicator == 0)
                                {
                                    if (rsrcPkgIntrvl >= 0)
                                    {
                                        simBkdCurUnconsByR[rsrcIntsIter] += dfuBean.getSimBookedByDfuCurPriceUncons()[rsrcPkgIntrvl] * packageUsage;
                                    }
                                } else
                                {
                                    simBkdCurUnconsByR[rsrcIntsIter] += dfuBean.getSimBookedByDfuCurPriceUncons()[lastintrvl] * packageUsage;
                                }
                            }                          
                        }
                    }
                }
                simAcceptedByRsrc = this.cfeControlsObj.getSimAcceptedByResrcPrice(rsrcBean);

                if (simAcceptedByRsrc == null)
                {
                    simAcceptedByRsrc = new double[rsrcBean.getRsrcIntName().length];
                }
                double dmdSurvAtResrcPrice = 0;
                double simRevenueByRsrc = 0;

                rsrcPkg = rsrcBean.getRscPkg();
                simRevenueByRsrcDfu = new double[rsrcPkg.length][][];
                for (int rsrcPkgIter = 0; rsrcPkgIter < rsrcPkg.length; rsrcPkgIter++)
                {
                    pkgBean = pkgBeanList.get(rsrcPkg[rsrcPkgIter]);
                    pkgDfus = pkgBean.getDfuPkg();
                    simRevenueByRsrcDfu[rsrcPkgIter] = new double[pkgDfus.length][];
                    for (int pkgDfusIter = 0; pkgDfusIter < pkgDfus.length; pkgDfusIter++)
                    {
                        dfuBean = dfuBeanList.get(pkgDfus[pkgDfusIter]);
                        int dfuLen = dfuBean.getPostDates().length;
                        simRevenueByRsrcDfu[rsrcPkgIter][pkgDfusIter] = new double[dfuLen];
                        simAcceptedByDfu = this.cfeControlsObj.getSimAcceptedByDfu(dfuBean);

                        for (int dfuIntIter = 0; dfuIntIter < dfuLen; dfuIntIter++)
                        {
                            //Commented the code to avoid calculating simAcceptedByRsrc twice.
                            //simAcceptedByRsrc[dfuIntIter] += simAcceptedByDfu[dfuIntIter];
                            dfuPricePerRscDate = this.cfeControlsObj.getDfuPriceForRevenueCalc(dfuBean, rsrcBean, dfuIntIter);
                            simRevenueByRsrcDfu[rsrcPkgIter][pkgDfusIter][dfuIntIter] = simAcceptedByDfu[dfuIntIter] * dfuPricePerRscDate + (dfuIntIter == 0 ? 0 : simRevenueByRsrcDfu[rsrcPkgIter][pkgDfusIter][dfuIntIter - 1] * dfuBean.getSurRate()[dfuIntIter]);
                        }
                        int dfuInts = simRevenueByRsrcDfu[rsrcPkgIter][pkgDfusIter].length;
                        simRevenueByRsrc += simRevenueByRsrcDfu[rsrcPkgIter][pkgDfusIter][dfuInts - 1];
                        dmdSurvAtResrcPrice += this.cfeControlsObj.getDmdSurvAtDfuPrice(dfuBean) * pkgBean.getUsage();
                    }
                }
                rsrcBean.setSimRevenueByRsrcDfu(simRevenueByRsrcDfu);
                rsrcBean.setSimBkdCurUnconsByR(simBkdCurUnconsByR);
                this.cfeControlsObj.setSimRevenueAtResrcPrice(rsrcBean, simRevenueByRsrc);
                double totalRevByResrcPrice = 0;
                double rpacAtResrcPrice = 0;

                double dmdByResrcPrice = 0;
                this.cfeControlsObj.calculateResrcMiscParameters(dfuBeanList, pkgBeanList, rsrcBean);

                double[] evalCap = rsrcBean.getRsrcCapacity();
                int postDtLen = rsrcBean.getRscDateIntName().length;
                double[] simAcceptedByResrc = this.cfeControlsObj.getSimAcceptedByResrcPrice(rsrcBean);
                for (int pdInd = 0; pdInd < postDtLen; pdInd++)
                {
                    dmdByResrcPrice += simAcceptedByResrc[pdInd];
                }
                this.cfeControlsObj.setDmdAtResrcPrice(rsrcBean, dmdByResrcPrice);

                totalRevByResrcPrice = (rsrcBean.getCurBkdRevByResrc() * rsrcBean.getMaterializationRate()) + simRevenueByRsrc;
                if (!CplexParamBean.isCfeUseAux())
                {
                    if (evalCap[postDtLen - 1] > 0)
                    {
                        rpacAtResrcPrice = totalRevByResrcPrice / evalCap[postDtLen - 1];
                    } else
                    {
                        rpacAtResrcPrice = totalRevByResrcPrice;
                    }
                } else
                {
                    if (rsrcBean.getAuxCapacity() > 0)
                    {
                        rpacAtResrcPrice = totalRevByResrcPrice / rsrcBean.getAuxCapacity();
                    } else
                    {
                        rpacAtResrcPrice = totalRevByResrcPrice;
                    }
                }
                this.cfeControlsObj.setTotalRevAtResrcPrice(rsrcBean, totalRevByResrcPrice);
                this.cfeControlsObj.setRPACAtResrcPrice(rsrcBean, rpacAtResrcPrice);
                this.cfeControlsObj.setAuxBkdByResrcPrice(rsrcBean, this.cfeControlsObj.getSimAuxBookedByResrcPrice(rsrcBean)[postDtLen - 1]);
                this.cfeControlsObj.setDmdSurvByResrcPrice(rsrcBean, dmdSurvAtResrcPrice);
            }
        }
        catch (Exception e)
        {
            Trace.write(Trace.error, " - error in  bookedAcceptedRsrc - ",e);
            throw new ManuRuntimeException(e);
        }
    }

}

/**
 *
 * $Log: NPOCfe.java,v $
 * Revision 1.2.68.11.2.1.2.12  2013/01/22 10:28:47  mnalla
 * modified exception code in bookingSurvival()
 *
 * Revision 1.2.68.11.2.1.2.11  2013/01/10 06:32:57  mnalla
 * removed unhandled null pointer exception in initialize()
 *
 * Revision 1.2.68.11.2.1.2.10  2013/01/07 06:16:19  mnalla
 * modified code for exception handling for price rounding rules
 *
 * Revision 1.2.68.11.2.1.2.9  2012/11/28 05:45:43  aanumolu
 * Modified to fix issues on PriceRoundingRules.
 *
 * Revision 1.2.68.11.2.1.2.8  2012/11/19 10:21:26  aanumolu
 * Added trace messages to log files
 *
 * Revision 1.2.68.11.2.1.2.7  2012/11/09 16:50:03  aanumolu
 * Updated/Added as part of new design change for running CFE Analysis from what-if UI.
 *
 * Revision 1.2.68.11.2.1.2.6  2012/10/16 04:51:01  aanumolu
 * Added/Modified for CFE modularization as part of Analyze mode feature development
 *
 * Revision 1.2.68.11.2.1.2.5  2012/08/23 06:47:50  aanumolu
 * Changed / added as part of PriceRoundingRules feature.
 *
 * Revision 1.2.68.11.2.1.2.4  2012/08/06 09:05:52  mnalla
 * modified aux usage data type to float
 *
 * Revision 1.2.68.11.2.1.2.3  2012/08/03 10:05:45  pdronadula
 * Optimized unused imports
 *
 * Revision 1.2.68.11.2.1.2.1  2012/05/24 02:47:30  sduraira
 * Post release code-formatting and optimization of imports
 *
 * Revision 1.2.68.11.2.1  2012/04/12 13:01:55  pdronadula
 * Made changes to achieve' Opt once Cplex twice' functionality for GEM
 *
 * Revision 1.2.68.11  2011/10/28 08:45:19  sravindran
 * Modified to remove array for demand types
 *
 * Revision 1.2.68.10  2011/10/17 09:55:12  sravindran
 * Modified to make dmdType dynamic
 *
 * Revision 1.2.68.9  2011/10/12 13:59:49  pdronadula
 * Add parameter for simPriceDfuCurForLog for putting it in .dat files for data verification
 *
 * Revision 1.2.68.8  2011/10/10 11:45:14  sravindran
 * added null check for demand type
 *
 * Revision 1.2.68.7  2011/10/10 06:24:31  mnalla
 * added BTC for resource
 *
 * Revision 1.2.68.6  2011/09/27 11:04:57  pdronadula
 * Made changes to use dfu effprice for cur controls calculations rather than using
 * constructed simPricebyDfu
 *
 * Revision 1.2.68.5  2011/06/06 11:31:33  mnalla
 * added code to populate simAuxBookedByRsrc()
 *
 * Revision 1.2.68.4  2011/06/06 09:22:22  mnalla
 * missing simAcceptedByRsrc initiation code is added
 *
 * Revision 1.2.68.3  2011/06/06 06:58:54  mnalla
 * modified code to include packageUsage
 *
 * Revision 1.2.68.2  2011/05/27 08:03:21  pdronadula
 * Changed to incorporate cfe changes at Resource level optimization for AG.
 *
 * Revision 1.2.68.1  2011/01/24 10:04:46  aanumolu
 * Merged TPO 7.6 changes to 7.7 branch.
 *
 * Revision 1.2.12.4  2010/12/03 09:40:53  rbadveeti
 * Exception Handling Related Changes
 *
 * Revision 1.2.12.3  2010/11/29 04:49:23  rbadveeti
 * For CVS Revision History Entry
 *
 *
 **/