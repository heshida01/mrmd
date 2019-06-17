package datamining.xmu.edu.cn;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.Map.Entry;

import weka.classifiers.evaluation.output.prediction.Null;
import weka.core.Instances;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.meta.Bagging;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.j48.*;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import weka.core.SelectedTag;


public class MRMD_mulan {

	static String [] classAttr;
	static double [][] feaData;
	static String [][] labelData;
	static List<Map.Entry<String, Double>> mrmrList;
	static String inputFile;
	static String outoputFile;
	static Calendar calendar = Calendar.getInstance();
	static int minutes = calendar.get(Calendar.MINUTE);
	static String arff = "out_"+ String.valueOf(minutes)+"."+"arff";
	static int insNum = 0;
	static int feaNum = 0;
	static int labNum = 1;
	static int seleFeaNum = 0;
	static int disFunc = 1;
	static double bestRate=0;
	static double auc=0;
	static boolean isAuto = true;
	private static int optNum;
	private static String model="rf";
	static ArrayList<String> arrayList;
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		// ?????��??
//String x="-i D://out.arff -o D://gjs1.txt -sn 20";
//args=x.split(" ");
//
		// Create a Parser
		CommandLineParser parser = new BasicParser( );
		Options options = new Options( );
		options.addOption("h", "help", false, "Print this usage information");
		options.addOption("i", "input", true, "input file" );
		options.addOption("o", "output", true, "output file  score rank of each feature");
		options.addOption("df", "disFuc", true, "the distance function default(1)" );
		options.addOption("a", "arff", true, "outputfile of arff default (opt.arff)");
		options.addOption("m", "model", true, "opt model type defauly(rf)" );
		options.addOption("N", "No", false, "auto select" );
		options.addOption("sn", "sn", true, "select feature number");


		// Parse the program arguments
		CommandLine commandLine = parser.parse( options, args );

		// Set the appropriate variables based on supplied options
		String file = "";


		if(commandLine.hasOption('N')) {
			isAuto= false;
		}

		if(commandLine.hasOption("sn")) {
			seleFeaNum = Integer.parseInt(commandLine.getOptionValue("sn"));

		}

		if(commandLine.hasOption('h') ) {
			print_help();
			System.exit(0);
		}
		if(!commandLine.hasOption('i') || !commandLine.hasOption('o')) {
			System.out.println("You should input -i , -o ");
			System.exit(0);
		}
		outoputFile = commandLine.getOptionValue('o');
		inputFile = commandLine.getOptionValue('i');
		if( commandLine.hasOption("a") ) {
			arff = commandLine.getOptionValue("a");
		}
		if( commandLine.hasOption("m") ) {
			model = commandLine.getOptionValue("m");
			if (model.equals("rf") || model.equals("svm") || model.equals("bagging") ||model.equals("N")){
				;
			}else{
				System.out.println("model only can be rf,svm,bagging,N\n");
				System.exit(0);
			}


		}
		if( commandLine.hasOption("df") ) {
			disFunc = Integer.parseInt(commandLine.getOptionValue("df"));
			if(disFunc < 1 || disFunc > 4)
			{
				System.out.println("\r\nThe parameter of -df is error !! df={1, 2, 3, 4}\r\n");
			}
		}



		File InputFile = new File(inputFile);
		if(!InputFile.exists())
		{
			System.out.println("Can't find input file: " + InputFile);
			System.exit(0);
		}
		BufferedReader InputBR = new BufferedReader(new InputStreamReader(new FileInputStream(InputFile), "utf-8"));
		String InputLine = InputBR.readLine();
		String[] dataString;


		while(!InputLine.toUpperCase().contains("@DATA"))
		{
			InputLine = InputBR.readLine();
		}
		InputLine = InputBR.readLine();


		while(InputLine != null)
		{
			if(InputLine.equals("")){
				InputLine = InputBR.readLine();
				continue;
			}

			if(insNum==1)
				feaNum = InputLine.split(",").length;
			insNum ++;
			InputLine = InputBR.readLine();
		}


		InputBR.close();
		feaNum=feaNum-labNum;
		if(seleFeaNum==0){
			seleFeaNum = feaNum;
		}


		String [][] inputData = new String[insNum][feaNum + labNum];
		getData gd = new getData();
		gd.setData(inputData);
		gd.setFeaNum(feaNum);
		gd.setInsNum(labNum);
		gd.setLabNum(labNum);

		gd.run(inputFile);

		double[] PearsonValue = new double[feaNum];
		Pearson pd = new Pearson(feaNum, labNum);
		pd.setInsNum(insNum);
		pd.setFeaNum(feaNum);
		pd.setLabNum(labNum);
		pd.setData(inputData);
		pd.setPearsonValue(PearsonValue);
		pd.run();

		labelData = new String[insNum][labNum];
		getLabel gl = new getLabel();
		gl.setData(labelData);
		gl.setFeaNum(feaNum);
		gl.setInsNum(insNum);
		gl.setLabNum(labNum);
		gl.run(inputData);

		feaData = new double[insNum][feaNum];
		getFeaData gfd = new getFeaData();
		gfd.setData(feaData);
		gfd.setFeaNum(feaNum);
		gfd.setInsNum(insNum);
		gfd.setLabNum(labNum);
		gfd.run(inputData);

		inputData = null;

		double[] CosineValue = new double[feaNum];
		Cosine cd = new Cosine(feaNum);
		cd.setCosineValue(CosineValue);
		cd.setData(feaData);
		cd.setFeaNum(feaNum);
		cd.setInsNum(insNum);
		cd.setLabNum(labNum);
//		cd.run();

		double[] EuclideanValue = new double[feaNum];
		Euclidean ed = new Euclidean(feaNum);
		ed.setData(feaData);
		ed.setEuclideanValue(EuclideanValue);
		ed.setFeaNum(feaNum);
		ed.setInsNum(insNum);
//		ed.setLabNum(labNum);

		double[] TanimotoValue = new double[feaNum];
		Tanimoto td = new Tanimoto(feaNum);
		td.setData(feaData);
		td.setTanimotoValue(TanimotoValue);;
		td.setFeaNum(feaNum);
		td.setInsNum(insNum);
//		td.setLabNum(labNum);

		double[] mrmrValue = new double[feaNum];
		switch (disFunc)
		{
			case 1:
				ed.run();
				for(int i = 0; i < feaNum; ++ i)
				{
					mrmrValue[i] = EuclideanValue[i] + PearsonValue[i];
				}
				break;
			case 2:
				cd.run();
				for(int i = 0; i < feaNum; ++ i)
				{
					mrmrValue[i] = CosineValue[i] + PearsonValue[i];
				}
				break;
			case 3:
				td.run();
				for(int i = 0; i < feaNum; ++ i)
				{
					mrmrValue[i] = TanimotoValue[i] + PearsonValue[i];
				}
				break;
			case 4:
				ed.run();
				cd.run();
				td.run();
				for(int i = 0; i < feaNum; ++ i)
				{
					mrmrValue[i] = (PearsonValue[i] * 3 + EuclideanValue[i] + CosineValue[i] + TanimotoValue[i])/3;
				}
				break;
			default:
				break;
		}

		Instances data = new Instances(new BufferedReader(new FileReader(inputFile)));
		data.setClassIndex(data.numAttributes() - 1);
		// randomize data
		Random rand = new Random(1);
		Instances randData = new Instances(data);
		randData.randomize(rand);


		Enumeration data_e = data.enumerateAttributes();
		arrayList = new ArrayList<String>(21);
		int index = 0;
		while(data_e.hasMoreElements())
		{
			String str = data_e.nextElement().toString();
			str = str.split(" ")[1];
			arrayList.add(str);
		}
		//System.out.println(arrayList);

		double max=0;
		for (int i=0;i<mrmrValue.length;i++){
			if (mrmrValue[i]>max){
				max=mrmrValue[i];
			}
		}

		for (int i=0;i<mrmrValue.length;i++){
			mrmrValue[i]=mrmrValue[i]/max;
		}


		Map<String, Double> mrmrMap = new HashMap<String, Double>();
		mrmrList = new ArrayList<Map.Entry<String, Double>>(mrmrMap.entrySet());
		mrmrList = initialHashMap(mrmrValue, feaNum);


		BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outoputFile), false), "utf-8"));
		bufferedWriter.write("The number of selected features is: " + seleFeaNum + "\r\n\r\n");
		//bufferedWriter.write("The index of selected features start from 0" + "\r\n\r\n");
		bufferedWriter.write("Num" + "		" + "FeaName" + "		" + "Score" + "\r\n\r\n");
		int line = 1;
		for(int i = 0; i < seleFeaNum; ++ i)
		{
			bufferedWriter.write(line + "		" + mrmrList.get(i).getKey() + "		" + mrmrList.get(i).getValue() + "\r\n");
			line ++;
		}
		bufferedWriter.flush();
		bufferedWriter.close();

		classAttr = new String[labNum];
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(inputFile)), "utf-8"));
		String lineString = bufferedReader.readLine();
		while(!lineString.contains("@relation") && !lineString.contains("@RELATION") && !lineString.contains("@Relation"))
		{
			lineString = bufferedReader.readLine();
		}
		lineString = bufferedReader.readLine();
		while(lineString.length() == 0)
		{
			lineString = bufferedReader.readLine();
		}
		int count = 0;
		while(count < feaNum)
		{
			if(lineString.length() != 0)
			{
				count ++;
				lineString = bufferedReader.readLine();
			}
			else
			{
				lineString = bufferedReader.readLine();
			}
		}
		while(lineString.length() == 0)
		{
			lineString = bufferedReader.readLine();
		}
		count = 0;
		while(count < labNum)
		{
			if(lineString.length() != 0)
			{

				classAttr[count] = lineString;
				count ++;
				lineString = bufferedReader.readLine();
			}
			else
			{
				lineString = bufferedReader.readLine();
			}
		}
		bufferedReader.close();

		System.out.println("MRMD over.");
		System.out.println("Feature selction optimation begin");
		System.out.println("model:"+model);

		if(isAuto){
			int num = optSelect();
			writeFeature(num);
			System.out.println("The best feature number: "+String.valueOf(optNum));
			System.out.println("The best rate: "+String.valueOf(bestRate));
			System.out.println("Feature selction optimation end,the best arff save "+arff);
		}

	}



	public static void print_help(){
		System.out.println("Usage: java -jar MRMD.jar" +
				"-i inputFile " +
				"-o outputFile \n" +
				"-df disFunc default(1) \n" +
				"-a arff default(opt.arff) \n" +
				"-model rf default(rf)  \n");
		System.out.println("[-i -o] is Necessary   [-sn -ln -df -a - model] is Optional ");
		System.exit(0);
	}

	public static int optSelect() throws Exception{
		double max=0;
		optNum=1;
		double temp=0;
		for(int i=1;i<= seleFeaNum;i++){
			try {
				writeFeature(i);
				temp = featureRate();
				System.out.println("feature num: "+i+" rate: "+temp);
				if (temp>max){
					max=temp;
					optNum=i;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		bestRate=max;
		writeFeature(optNum);
		return optNum;
	}

	public static void writeFeature(int seleFeaNum) throws IOException{
		BufferedWriter arffWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(arff), false), "utf-8"));
		arffWriter.write("@relation " + (new File(inputFile)).getName() + "_feaSele");
		arffWriter.newLine();
		arffWriter.newLine();

		for(int i = 0; i < seleFeaNum; i ++)
		{
			//arffWriter.write("@attribute Fea" + i + " numeric");
			arffWriter.write("@attribute " +mrmrList.get(i).getKey() + " numeric");
			arffWriter.newLine();
		}
		for(int i = 0; i < labNum; i ++)
		{
			arffWriter.write(classAttr[i]);
			arffWriter.newLine();
		}
		arffWriter.write("\r\n@data\r\n\r\n");

		//	System.out.println("insNum" + insNum);

		for(int i = 0; i < insNum; i ++)
		{

			for(int j = 0; j < seleFeaNum; j ++)
			{
//				System.out.println(i);
				int index = arrayList.indexOf(mrmrList.get(j).getKey());
				arffWriter.write(feaData[i][index] + ",");

				//arffWriter.write(feaData[i][Integer.parseInt(mrmrList.get(j).getKey()]);
			}
//			arffWriter.write(feaData[i][Integer.parseInt(mrmrList.get(seleFeaNum - 1).getKey().substring(3, mrmrList.get(seleFeaNum - 1).getKey().length()))] + "\r\n");
			for(int j = 0; j < labNum - 1; j ++)
			{
				arffWriter.write(labelData[i][j] + ",");
			}
			arffWriter.write(labelData[i][labNum - 1]);
			arffWriter.newLine();
		}

		arffWriter.flush();
		arffWriter.close();
	}


	public static Classifier getClassifier(String name) throws Exception {
		Classifier classify = null;

		int enum_type=1;
		if(name.equals("rf")) enum_type = 1;
		if(name.equals("svm")) enum_type = 2;
		if(name.equals("bagging")) enum_type = 3;

		switch (enum_type){
			case 1:
				classify=new RandomForest();
				String[] options={"-I 100"};
				// weka.core.Utils.splitOptions('-C 0.25 -M 2')
				//((RandomForest) classify).setOptions(weka.core.Utils.splitOptions("-I 100 -K 0   -S 1"));
				((RandomForest) classify).setMaxDepth(0);
				((RandomForest) classify).setNumTrees(100);
				((RandomForest) classify).setNumFeatures(0);
				((RandomForest) classify).setSeed(1);
				break;
			case 2:
				classify=new LibSVM();
				((LibSVM) classify).setCacheSize(40.0);
				((LibSVM) classify).setCoef0(0.0);
				((LibSVM) classify).setCost(1.0);
				((LibSVM) classify).setDegree(3);
				((LibSVM) classify).setEps(0.001);
				((LibSVM) classify).setGamma(0.0);
				((LibSVM) classify).setKernelType(  new SelectedTag(2, LibSVM.TAGS_KERNELTYPE));
				((LibSVM) classify).setDoNotReplaceMissingValues(false);
				((LibSVM) classify).setLoss(0.1);
				((LibSVM) classify).setNormalize(false);
				((LibSVM) classify).setNu(0.5);
				((LibSVM) classify).setProbabilityEstimates(false);
				((LibSVM) classify).setShrinking(true);
				((LibSVM) classify).setSVMType(new SelectedTag(0, LibSVM.TAGS_SVMTYPE));
				break;
			case 3:
				classify=new Bagging();
				((Bagging) classify).setBagSizePercent(100);
				((Bagging) classify).setCalcOutOfBag(false);

				((Bagging) classify).setDebug(false);
				((Bagging) classify).setNumExecutionSlots(1);
				((Bagging) classify).setNumIterations(10);
				((Bagging) classify).setSeed(1);
				break;
			default:
				System.out.println("???��???��??rf(randomForst),svm,bagging");
		}
		return classify;
	}



	public static double featureRate() throws Exception{
		FileReader reader=new FileReader(arff);
		Instances  dataset=new Instances(reader);
		dataset.setClassIndex(dataset.numAttributes()-1);
		Classifier classify=getClassifier(model);
		int seed = 1;
		int folds = 10;
		// randomize data
		Random rand = new Random(seed);
		//create random dataset
		Instances randData = new Instances(dataset);
		randData.randomize(rand);
		//stratify
		Evaluation evalAll = new Evaluation(randData);
		if (randData.classAttribute().isNominal())
			randData.stratify(folds);
		if(insNum<10){
			folds=insNum;
		}

		//System.exit(0);
		for (int n = 0; n < folds; n++) {
			Evaluation eval = new Evaluation(randData);
			Instances train = randData.trainCV(folds, n);
			Instances test = randData.testCV(folds, n);
			// build and evaluate classifier
			classify.buildClassifier(train);
			eval.evaluateModel(classify, test);
			evalAll.evaluateModel(classify, test);
		}
		return evalAll.pctCorrect();
	}
	public static List initialHashMap(double data[], int feaNum)
	{
		Map<String, Double> mrmrMap = new HashMap<String, Double>();

		for(int i = 0; i < feaNum; ++ i)
		{
			String str = arrayList.get(i);

			mrmrMap.put(str , data[i]);

			//mrmrMap.put("Fea"+i , data[i]);
		}


		List<Map.Entry<String, Double>> mrmrList = new ArrayList<Map.Entry<String, Double>>(mrmrMap.entrySet());
		Collections.sort(mrmrList, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2)
			{
//				return (o1.getValue()).compareTo(o2.getValue());
				if(Double.parseDouble(o1.getValue().toString()) < Double.parseDouble(o2.getValue().toString()))
				{
					return 1;
				}
				else if(Double.parseDouble(o1.getValue().toString()) == Double.parseDouble(o2.getValue().toString()))
				{
					return 0;
				}
				else {
					return -1;
				}
			}

		});
		return mrmrList;
	}
}
