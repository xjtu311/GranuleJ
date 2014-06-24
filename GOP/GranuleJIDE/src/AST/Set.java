
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.Collection;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import java.util.Stack;import java.util.regex.Pattern;import java.util.HashMap;import java.util.Iterator;import java.io.FileOutputStream;import java.io.IOException;import java.io.FileInputStream;import java.security.MessageDigest;import java.security.NoSuchAlgorithmException;import javax.xml.transform.stream.StreamResult;import javax.xml.transform.dom.DOMSource;import javax.xml.transform.TransformerFactory;import javax.xml.transform.Transformer;import javax.xml.parsers.DocumentBuilder;import javax.xml.parsers.DocumentBuilderFactory;import org.w3c.dom.Element;import org.w3c.dom.Document;import java.util.Map.Entry;import javax.xml.transform.TransformerException;import javax.xml.parsers.ParserConfigurationException;

public class Set extends java.lang.Object {
    // Declared in Sets.jrag at line 346

    protected static HashMap elementMap = new HashMap();

    // Declared in Sets.jrag at line 347

    protected static ArrayList elementList = new ArrayList();

    // Declared in Sets.jrag at line 349

    
    protected static int index(Object element) {
      if(!elementMap.containsKey(element)) {
        elementMap.put(element, new Integer(elementList.size()));
        elementList.add(element);
      }
      return ((Integer)elementMap.get(element)).intValue();
    }

    // Declared in Sets.jrag at line 357

    
    protected static Object element(int index) {
      return elementList.get(index);
    }

    // Declared in Sets.jrag at line 362


    // represent set using a bit field
    int[] bits;

    // Declared in Sets.jrag at line 366


    // iterate over the set by searching the bit field linearly for one bits
    // use the array to perform the reverse lookup
    public Iterator iterator() {
      return new Iterator() {
        private int nextElement;
        {
          // make nextElement refer to the first element
          nextElement = 0;
          hasNext();
        }
        public boolean hasNext() {
          while((nextElement >> 5) < bits.length) {   // while (nextElement / 32 < bits.length)
            int offset = nextElement >> 5;            //   nextElement / 32
            int bit = 1 << (nextElement & 0x1f);      //   nextElement % 32
            if((bits[offset] & bit) == bit)           
              return true;
            nextElement++;
          }
          return false;
        }
        public Object next() {
          return element(nextElement++);
        }
        public void remove() {
          throw new Error("remove not supported for " + Set.this.getClass().getName());
        }
      };
    }

    // Declared in Sets.jrag at line 395


 	// Measurements

	private static int nbrOfCreatedSets = 0;

    // Declared in Sets.jrag at line 396

	public static int getNbrOfCreatedSets() { return nbrOfCreatedSets; }

    // Declared in Sets.jrag at line 398


	private static int nbrOfUnion = 0;

    // Declared in Sets.jrag at line 399

	public static int getNbrOfUnion() { return nbrOfUnion; }

    // Declared in Sets.jrag at line 401


    private static int nbrOfCompl = 0;

    // Declared in Sets.jrag at line 402

	public static int getNbrOfCompl() { return nbrOfCompl; }

    // Declared in Sets.jrag at line 406



    // the empty set
    public static Set empty() {
      return emptySet;
    }

    // Declared in Sets.jrag at line 410

    // override set operations when operating on the empty set 
    private static Set emptySet = new Set(0) {
      public Set union(Set set) {
		  nbrOfUnion++;

        return set;
      }
      public Set union(Object element) {
		  nbrOfUnion++;

        int index = index(element);
        int offset = index >>> 5;           // index / 32
        int bit = 1 << (index & 0x1f);     // index % 32
        Set set = new Set(offset + 1);
        set.bits[offset] = bit;
        return set;
      }
      public Set compl(Set set) {
	
		nbrOfCompl++;

        return this;
      }
      public Set compl(Object element) {

		nbrOfCompl++;

        return this;
      }
      public Set intersect(Set set) {
        return this;
      }
      public boolean isEmpty() {
        return true;
      }
    };

    // Declared in Sets.jrag at line 447

    
    // the full set
    public static Set full() {
      return fullSet;
    }

    // Declared in Sets.jrag at line 451

    // override set operations when operating on the full set 
    private static Set fullSet = new Set(0) {
      public Set union(Set set) {

		nbrOfUnion++;

        return this;
      }
      public Set union(Object element) {

     	nbrOfUnion++;

        return this;
      }
      public Set compl(Set set) {
    	  throw new Error("compl not supported for the full set");
       }
      public Set compl(Object element) {
        throw new Error("compl not supported for the full set");
      }
      public Set intersect(Set set) {
        return set;
      }
      public boolean isEmpty() {
        return false;
      }
    };

    // Declared in Sets.jrag at line 479


    // create a new set containing at most size * 32 elements
    protected Set(int size) {
		nbrOfCreatedSets++;
      bits = new int[size];
    }

    // Declared in Sets.jrag at line 484

    // create a new set containing at most size * 32 elements and copy elements from set
    protected Set(Set set, int size) {
      this(size);
      for(int i = 0; i < set.bits.length; i++)
        bits[i] = set.bits[i];
    }

    // Declared in Sets.jrag at line 490

    // create a copy of set
    protected Set(Set set) {
      this(set, set.bits.length);
    }

    // Declared in Sets.jrag at line 494


    public Set union(Set set) {

      nbrOfUnion++;

      Set min, max;
      if(bits.length >= set.bits.length) {
        max = this;
        min = set;
      }
      else {
        max = set;
        min = this;
      }
      int length = min.bits.length;
      int i = 0;
      // search for elements in the smaller set that are missing in the larger set
      while(i < length && (max.bits[i] & min.bits[i]) == min.bits[i])
        i++;
      if(i != length) {
        // copy the larger set and store missing elements from smaller set
        max = new Set(max);
        for(; i < length; i++)
          max.bits[i] |= min.bits[i];
      }
      return max;
    }

    // Declared in Sets.jrag at line 521

    
    public Set union(Object element) {

 		nbrOfUnion++;

      int index = index(element);
      int offset = index >> 5;
      int bit = 1 << (index & 0x1f);
      if(bits.length > offset && (bits[offset] & bit) == bit)
        return this;
      // copy the set and store the missing element
      Set set = new Set(this, Math.max(offset + 1, bits.length));
      set.bits[offset] |= bit;
      return set;
    }

    // Declared in Sets.jrag at line 536

    
    public Set compl(Set set) {

      nbrOfCompl++;

	  // copy this
	  Set res = new Set(this);
	  int i = 0;
	  int length = Math.min(bits.length, set.bits.length);
	  // find first hit
	  while (i < length && (bits[i] & set.bits[i]) == 0) 
		  i++;
	  if (i != length) {
		 // remove 
		 for (; i < length; i++) {
			res.bits[i] &= ~set.bits[i];
		 }	 
	  }
	  return res;
    }

    // Declared in Sets.jrag at line 556

    
    public Set compl(Object element) {

		nbrOfCompl++;

      int index = index(element);
      int offset = index >> 5;
      int bit = 1 << (index & 0x1f);
      if(bits.length > offset && (bits[offset] & bit) == 0)
        return this;
      // copy the set and remove element
      Set set = new Set(this, Math.max(offset + 1, bits.length));
      set.bits[offset] &= ~bit;
      return set;
    }

    // Declared in Sets.jrag at line 571

    
    public Set intersect(Set set) {
      if(this.equals(set))
        return this;
      if(set == fullSet) return this;
      int length = Math.max(this.bits.length, set.bits.length);
      Set newSet = new Set(length);
      for(int i = 0; i < length; i++)
        newSet.bits[i] = this.bits[i] & set.bits[i];
      return newSet;
    }

    // Declared in Sets.jrag at line 582


    public boolean contains(Object o) {
      int index = index(o);
      int offset = index >> 5;
      int bit = 1 << (index & 0x1f);
      return offset < bits.length && (bits[offset] & bit) != 0;
    }

    // Declared in Sets.jrag at line 589


    public boolean equals(Object o) {
      if (o == null) return false;
      if(this == o) return true;
      if(o instanceof Set) {
        Set set = (Set)o;
        int length = set.bits.length > bits.length ? bits.length : set.bits.length;
        int i = 0;
        for(; i < length; i++)
          if(bits[i] != set.bits[i])
            return false;
        for(; i < bits.length; i++)
          if(bits[i] != 0)
            return false;
        for(; i < set.bits.length; i++)
          if(set.bits[i] != 0)
            return false;
        return true;
      }
      return super.equals(o);
    }

    // Declared in Sets.jrag at line 610


    public boolean isEmpty() {
      for(int i = 0; i < bits.length; i++)
        if(bits[i] != 0)
          return false;
      return true;
    }


}
