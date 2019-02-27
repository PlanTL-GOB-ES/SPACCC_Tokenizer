package Tokenization;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

/*
 * This script tokenizes all sentences of a text file.
 * Input:
 * - Text file, with one sentence per lilne.
 * - Tokenization model.
 * Output: 
 * - All tokens of the text file per line, separated by sentences ("=" indicates the end of the sentence).
 * 
 * This script only works with tokenization models created with Apache OpenNLP.
 */

public class Tokenization {

	private String file;
	private String model;
	
	private List<String> fullText;
	private Tokenizer tokenizer;
	
	public Tokenization(String file, String model)
	{
		this.file = file;
		this.model = model;
		
		fullText = new ArrayList<String>();
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		try
		{
			if (args.length != 2)
			{
				System.out.println("USAGE:\t" + "java -jar Tokenizer.jar TXT_FILE MODEL_FILE");
			}
			else
			{
				String file = args[0];
				String model = args[1];

				Tokenization sentenceSplitter = new Tokenization(file, model);
				sentenceSplitter.start();
			}			
		}
		catch (Exception e)
		{
			System.out.println("USAGE:\t" + "java -jar Tokenizer.jar TXT_FILE MODEL_FILE");
			e.printStackTrace();
		}		
	}

	public void start() throws IOException
	{
		loadModel();
		readTextFile();
		tokenize();
	}
	
	public void loadModel() throws FileNotFoundException, IOException
	{
		InputStream modelIn = new FileInputStream(model);
		TokenizerModel tokenizerModel = new TokenizerModel(modelIn);
		tokenizer = new TokenizerME(tokenizerModel);
	}
	
	public void readTextFile() throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
	    String line = "";
	    while ((line = reader.readLine()) != null)
	    {
	    	fullText.add(line);
	    }
	    reader.close();
	}
	
	public void tokenize()
	{
		for (int j = 0; j < fullText.size(); j++)
	    {
	    	String origLine = fullText.get(j);
	    	String[] tokens = tokenizer.tokenize(origLine);

	    	for(int k = 0; k < tokens.length; k++)
	    	{
	            System.out.println(tokens[k]);
	        }
	    	System.out.println("=============");
	    }
	}
}
