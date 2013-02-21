import java.util.*;

public class SentenceReverse
{
   public static void main(String args[])
   {
      Scanner in = new Scanner(System.in);
      String sentence = "";
      if( in.hasNextLine() )
      {
	 sentence = in.nextLine();
      }
     
      String reversed = reverseSentence(sentence);
      System.out.println(reversed);
      String searchReversed = reverseSentenceBySearch(sentence);
      System.out.println(searchReversed);
   }

   public static String reverseSentence(String sentence)
   {
      StringBuilder bld = new StringBuilder();
      String[] parts = sentence.split(" ");
      for ( int i = parts.length - 1; i>=0; i-- )
      {
	 bld.append(parts[i]);
	 if ( i != 0 )
	 {
	    bld.append(" ");
	 }
      }
      return bld.toString();
   }

   public static String reverseSentenceBySearch(String sentence)
   {
      
      char[] input = sentence.toCharArray();
      char[] output = new char[input.length];
      int outputPointer = 0;
      int wordEnd = -1;
      for ( int i = input.length-1; i >= 0; i-- )
      {
	 if ( wordEnd == -1 && input[i] != ' ' )
	 {
	    wordEnd = i;
	 }
	 if ( input[i] == ' ' || i == 0)
	 {
	    int cur = i;
	    if ( i != 0)
	    {
	       cur = i+1;
	    }
	    while ( cur <= wordEnd )
	    {
	       output[outputPointer] = input[cur];
	       cur++;
	       outputPointer++;
	    }
	    if ( i != 0 )
	    {
	       output[outputPointer] = input[i]; // append the space
	       outputPointer++;
	    }
	    wordEnd = -1;
	 }
      }
      StringBuilder bld = new StringBuilder();
      for ( int i = 0; i < output.length; i++ )
      {
	 bld.append(output[i]);
      }
      return bld.toString();
   }
}
