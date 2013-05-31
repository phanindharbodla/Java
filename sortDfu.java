    public void sortDfuOrder(String sortType, int simDate)  throws ManuRuntimeException
    {
        List<DfuBean> dfuBeanList = this.npoOptimzeData.getDfuList();
        List<Pair> dfuBeanSimPriceIdx = new ArrayList();
        Pair dfuBeanSimPriceIdxPair = null;
        DfuBean dfuBean = null;
        double[] simPrices = null;
        double simPriceForSimDate;
        Double [] simPriceArr = null;
        int simPriceArrIdx = 0;
        int simPriceArrLen = 0;
        try
        {
        	List<Double> temp =new ArrayList();
            /* for (int dfuBeanListIter = 0; dfuBeanListIter < dfuBeanList.size(); dfuBeanListIter++)
            {
                dfuBean = dfuBeanList.get(dfuBeanListIter);//get a bean 
                simPrices = dfuBean.getSimPriceByDfu();// 
                if (simPrices.length > simDate)
                {
                    simPriceForSimDate = simPrices[simDate];
                    if (simPriceForSimDate >= 0)
                    {
                        simPriceArrLen++;
                    }
                }
            }
            simPriceArr = new Double[simPriceArrLen];
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
                                dfuBean.setFxPriceMult(0.001); //This is to eliminate divide by zero error.   
                            }
                            simPriceForSimDate = simPriceForSimDate / dfuBean.getFxPriceMult();
                        } else if (sortType.equalsIgnoreCase(NPOProcessConstants.SORT_METHOD_OPTIMISTIC))
                        {
                            simPriceForSimDate = simPriceForSimDate * dfuBean.getFxPriceMult();
                        }
                        simPriceArr[simPriceArrIdx] = simPriceForSimDate;
                        dfuBeanSimPriceIdxPair = new Pair(dfuBeanListIter, simPriceForSimDate);
                        dfuBeanSimPriceIdx.add(dfuBeanSimPriceIdxPair);
                        simPriceArrIdx++;
                    }
                }
            } */
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
                                dfuBean.setFxPriceMult(0.001); //This is to eliminate divide by zero error.   
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