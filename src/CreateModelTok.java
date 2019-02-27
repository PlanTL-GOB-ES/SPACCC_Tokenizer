package Tokenization;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;

import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.ml.EventTrainer;
import opennlp.tools.tokenize.TokenSample;
import opennlp.tools.tokenize.TokenSampleStream;
import opennlp.tools.tokenize.TokenizerFactory;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.ModelType;

/*
 * This class is used to create the tokenization model for Spanish clinical cases using the Apache OpenNLP API.
 * Input:
 * - Training file, all documents tokenized in a single file.
 * - Path where the final model will be printed.
 * - Name of the created model's file.
 * - File with a list of abbreviations, one abbreviation per line.
 * We recommend using the abbreviations' file in order to get a better model.
 * Output:
 * - Tokenization model.
 */

public class CreateModelTok {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String trainFile = args[0];
		String outModel = args[1];
		String modelName = args[2];
		String abbrFile = args[3];
		
		// Load dictionary of abbreviations.
		Dictionary abbrDictionary = makeAbbrDictionary(abbrFile);
		
		Charset charset = Charset.forName("UTF-8");		

		// Load train set.
		MarkableFileInputStreamFactory factory = new MarkableFileInputStreamFactory(new File(trainFile));
		ObjectStream<String> lineStream = new PlainTextByLineStream(factory, charset);
		ObjectStream<TokenSample> sampleStream = new TokenSampleStream(lineStream);

		TokenizerModel model;

		try 
		{
			// Parameters used by machine learning algorithm, Maxent, to train its weights.
	        TrainingParameters mlParams = new TrainingParameters();
	        mlParams.put(TrainingParameters.ITERATIONS_PARAM, Integer.toString(1500));	
	        mlParams.put(TrainingParameters.CUTOFF_PARAM, Integer.toString(4)); 		
	        mlParams.put(TrainingParameters.TRAINER_TYPE_PARAM, EventTrainer.EVENT_VALUE);
	        mlParams.put(TrainingParameters.ALGORITHM_PARAM, ModelType.MAXENT.name());	
			
	        // Train the model.
	        // We set Alpha-Numerical Optimization to false, in order to get better results.
	        TokenizerFactory tokenizerFactory = new TokenizerFactory("es", abbrDictionary, false, null);
			model = TokenizerME.train(sampleStream, tokenizerFactory, mlParams);
		} 
		finally 
		{
			sampleStream.close();
		}

		
		OutputStream modelOut = null;
		try 
		{
			// Print out the model into a file.
			modelOut = new BufferedOutputStream(new FileOutputStream(outModel + File.separator + modelName));
			model.serialize(modelOut);
		} 
		finally 
		{
			if (modelOut != null)
		    modelOut.close();
		}
	}

	/*
	 * This method reads the abbreviation list file and loads the complete list into a dictionary.
	 */
	public static Dictionary makeAbbrDictionary(String abbrFile) throws IOException
	{
		Dictionary dictionary = new Dictionary();
		
		Reader reader = new BufferedReader(new FileReader(abbrFile));		
		dictionary = Dictionary.parseOneEntryPerLine(reader);
		
		return dictionary;
	}
}
