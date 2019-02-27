package Tokenization;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

/*
 * This class evaluates the performance of the tokenization model created with the class CreateModelTok.
 * Input:
 * - Test set folder (one file per document).
 * - Gold standard folder, one file per document and one token per line.
 * - Folder to print the results of the tokenization process with the created model, one token per line.
 * - Model file.
 * Output:
 * - Files tokenized.
 * - Evaluation of the tokenizer, showing the number of documents that match with the gold standard, with exactly the same output,
 * and the number of tokens per document that match with the gold tokens.
 */

public class EvaluateModelTok {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String testDir = args[0];
		String gsDir = args[1];
		String out = args[2];
		String modelFile = args[3];
		
		// Load the model
		InputStream modelIn = new FileInputStream(modelFile);
		TokenizerModel model = new TokenizerModel(modelIn);
		Tokenizer tokenizer = new TokenizerME(model);
		
		// Variables for statistics.
		// Document level evaluation.
		double totalFiles = 0;
		double totalWrong = 0;
		double totalCorrectTokensIncluded = 0;
		double more = 0;
		double less = 0;
		
		// Sentence-level evaluation, used to get Precision, Recall and F-Measure.
		int totalTokens = 0;
		int totalGStokens = 0;
		int tokensCorrect = 0;
		int tokensWrong = 0;
		
		/*
		 * Load each test file, split the sentences, and evaluate the output against the gold standard.
		 */
		File testFiles[] = new File(testDir).listFiles();
		for (int i = 0; i < testFiles.length; i++)
		{
			String name = testFiles[i].getName();
			
			// Load the test file.
			List<String> fullText = new ArrayList<String>();
			BufferedReader reader = new BufferedReader(new FileReader(testDir + File.separator + name));
		    String line = "";
		    while ((line = reader.readLine()) != null)
		    {
		    	fullText.add(line);
		    }
		    reader.close();
			
		    // Load the Gold Standard file.
		    String gsName = name;
			List<String> gsTokensList = new ArrayList<String>();
			BufferedReader reader2 = new BufferedReader(new FileReader(gsDir + File.separator + gsName));
		    String line2 = "";
		    while ((line2 = reader2.readLine()) != null)
		    {
		    	gsTokensList.add(line2);
		    }
		    reader2.close();
		    String[] gsTokens = new String[gsTokensList.size()];
		    gsTokens = gsTokensList.toArray(gsTokens);		    
	         
		    // Tokenize sentences of the testing set and write the results in a new file.
		    int testTokens = 0;
		    List<String> testTokenList = new ArrayList<String>();
		    BufferedWriter writer = new BufferedWriter(new FileWriter(out + File.separator + gsName));
		    for (int j = 0; j < fullText.size(); j++)
		    {
		    	String origLine = fullText.get(j);
		    	// Tokenize the sentence and save all tokens in an array.
		    	String[] tokens = tokenizer.tokenize(origLine);
		    	for (int k = 0; k < tokens.length; k++)
		    	{
		    		writer.write(tokens[k] + "\n");
		    		testTokenList.add(tokens[k]);
		    		testTokens++;
		    	}		    	
		    }
		    writer.close();
		    
		    // Total tokens in the testing set.
		    totalTokens = totalTokens + testTokenList.size();
		    // Total tokens in the Gold Standard.
		    totalGStokens = totalGStokens + gsTokensList.size();
		    
		    // Check tokens' lists
		    totalFiles++;
		    if (testTokens == gsTokens.length)
		    {		    	
		    	// Test and Gold Standard have the same length. High probability of guess.
		    	if (gsTokensList.equals(testTokenList))
		    	{
		    		// Test and Gold Standard are equal, full guess.
		    		totalCorrectTokensIncluded++;
		    		tokensCorrect = tokensCorrect + gsTokensList.size();
		    	}
		    	else
		    	{
		    		// Test and Gold Standard are not equal.
		    		totalWrong++;
		    		
		    		// TODO
		    		// Check the number of sentences that match with the gold Standard.
		    		// (We didn't reach this situation when creating and evaluating the model).
		    	}
		    }
		    else
		    {
		    	// Test and Gold Standard do not have the same length.
		    	totalWrong++;
		    	System.out.println("Document " + name + " splitting does not match: " + testTokens + " vs " + gsTokens.length);
		    	
		    	// Compare the number of tokens in the Gold Standard and test set.
		    	if (testTokens > gsTokens.length)
			    {
		    		more++;
			    }
		    	else if (testTokens < gsTokens.length) 
		    	{
		    		less++;
		    	}
		    	
		    	// Check the number of tokens that match with the Gold Standard.
		    	// Variables "j" and "k" indicate the positions of the current tokens we are analyzing.
		    	// "j" for the test set position.
		    	// "k" for the gold standard position.
		    	int k = 0; 
		    	for (int j = 0; j < testTokenList.size(); j++)
		    	{
		    		String testToken = testTokenList.get(j);
		    		String gsToken = gsTokensList.get(k);
		    		if (testToken.equals(gsToken))
		    		{
		    			// We found the same token in test and GS
		    			tokensCorrect++;
		    			k++;
		    		}
		    		else
		    		{
		    			// Tokens don't match in test and GS
		    			/*
		    			 * It could happen that the tokens in the test set were not correctly tokenized,
		    			 * or they were tokenized when there was no need for it. 
		    			 * 
		    			 * In this algorithm, we consider that the correct Gold token could be inside the test token if the 
		    			 * test token is longer. Or the test token could be inside the Gold token if the Gold one is longer.
		    			 * 
		    			 * The following algorithm checks the next token after the failed ones, and update the positions after detecting the
		    			 * correct ones.  
		    			 */
		    			
		    			tokensWrong++;
		    			System.out.println("\t" + testToken);
		    			
		    			// In case we reached the end of the document, and the last tokens don't match, end the loop.
		    			if (k+1 == gsTokensList.size() || j+1 == testTokenList.size())
		    			{
		    				break;
		    			}
		    			
		    			if (testToken.contains(gsToken))
		    			{
		    				/*
		    				 * The token in the testing set is longer than the gold token, the gold token is part of the test token.
		    				 */
		    				int before = k;
		    				k++;
		    				String nextTest = testTokenList.get(j+1);	// Get the next test token.
		    				String nextGS = gsTokensList.get(k);		// Get the next gold token.
		    				
		    				boolean end = false;
		    				// Check the next token in the gold standard and stop when we meet the token that matches the one with the test.
		    				while (!nextTest.equals(nextGS))
		    				{
		    					k++;		    					
		    					try
		    					{
		    						nextGS = gsTokensList.get(k);
		    					}
		    					catch (Exception e)
		    					{
		    						// We didn't reach the token we were looking for, continue normally without updating the positions.
		    						end = true;
		    						break;
		    					}
		    				}
		    				if (end)
		    				{
		    					k = before;
		    				}
		    			}
		    			else if (gsToken.contains(testToken))
		    			{
		    				/*
		    				 * The token in the gold standard is longer than the test token, the test token is part of the gold sentence.
		    				 */
		    				int before = j;
		    				j++;
		    				String nextTest = testTokenList.get(j);		// Get the next test token.
		    				String nextGS = gsTokensList.get(k+1);		// Get the next gold token.
		    				
		    				boolean end = false;
		    				// Check the next token in the test set and stop when we meet the token that matches the one with the gold standard.
		    				while (!nextTest.equals(nextGS))
		    				{
		    					j++;
		    					try
		    					{
		    						nextTest = testTokenList.get(j);
		    					}
		    					catch (Exception e)
		    					{
		    						// We didn't reach the token we were looking for, continue normally without updating the positions.
		    						end = true;
		    						break;
		    					}
		    				}
		    				if (!end)
		    				{
		    					j--;
		    				}
		    				else
		    				{
		    					j = before;
		    				}
		    				k++;
		    			}
		    			else
		    			{ 	
		    				/*
		    				 * Gold and test tokens do not match and are not part of each other.
		    				 * Depending on the number of tokens of test and gold, update the positions of current tokens for 
		    				 * the set with more tokens.
		    				 */
		    				if (testTokenList.size() > gsTokensList.size())
		    				{
		    					j++;
		    				}
		    				else if (testTokenList.size() < gsTokensList.size()) 
		    				{
		    					k++;
		    					j--;
		    				}
		    				else
		    				{
		    					k++;
		    				}
		    			}
		    		}
		    	}
		    }
		}
		
		/*
		 * Display the evaluation results for document level and token level.
		 */
		
		// The following lines show the number of documents that have equal tokenization results.
		System.out.println("Total documents correct with same tokens: " + totalCorrectTokensIncluded);
		double percentSameTokens = totalCorrectTokensIncluded * 100 / totalFiles;
		System.out.println("Results by document with same tokens: " + percentSameTokens + "%");
		System.out.println("Total documents wrong: " + totalWrong);
		System.out.println("Total documents: " + totalFiles);
		System.out.println("More tokens generated in test: " + more);
		System.out.println("More tokens found in gold standard: " + less);
		
		// The following lines show the number of tokens that have been correctly tokenized,
		// together with the Precision, Recall and F-Measure.
		System.out.println("Tokens in Gold Standard: " + totalGStokens);
		System.out.println("Tokens in Test: " + totalTokens);
		System.out.println("Tokens correct: " + tokensCorrect);
		System.out.println("Tokens wrong: " + tokensWrong);
		double precision = tokensCorrect * 100 / totalTokens;
		double recall = tokensCorrect * 100 / totalGStokens;
		double F1 = (2 * precision * recall) / (precision + recall);
		System.out.println("Precision: " + precision);
		System.out.println("Recall: " + recall);
		System.out.println("F1: " + F1);
	}

}
