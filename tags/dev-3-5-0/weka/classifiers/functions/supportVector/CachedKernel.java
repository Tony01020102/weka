package weka.classifiers.functions.supportVector;

import weka.core.*;

/**
 * Base class for RBFKernel and PolyKernel that implements a simple LRU.
 * (least-recently-used) cache if the cache size is set to a value > 0.
 * Otherwise it uses a full cache.
 * 
 * @author Eibe Frank (eibe@cs.waikato.ac.nz)
 * @author Shane Legg (shane@intelligenesis.net) (sparse vector code)
 * @author Stuart Inglis (stuart@reeltwo.com) (sparse vector code)
 * @author J. Lindgren (jtlindgr{at}cs.helsinki.fi) (RBF kernel)
 * @author Steven Hugg (hugg@fasterlight.com) (refactored, LRU cache)
 * @author Bernhard Pfahringer (bernhard@cs.waikato.ac.nz) (full cache)
 * @version $$
 */
public abstract class CachedKernel extends Kernel {
    
  /** Counts the number of kernel evaluations. */
  private int m_kernelEvals = 0;

  /** Counts the number of kernel cache hits. */
  private int m_cacheHits = 0;

  /** The size of the cache (a prime number) */
  private int m_cacheSize;

  /** Kernel cache */
  private double[] m_storage;
  protected long[] m_keys;

  /** The kernel matrix if full cache is used (i.e. size is set to 0) */
  private double[][] m_kernelMatrix;

  /** The number of instance in the dataset */
  private int m_numInsts;

  /** number of cache slots in an entry */
  private int m_cacheSlots = 4;


  /**
   * Initializes the kernel cache. The actual size of the cache in bytes is
   * (64 * cacheSize).
   */
  protected CachedKernel(Instances data, int cacheSize) {
    m_data = data;
    m_cacheSize = cacheSize;
    if (cacheSize > 0) {

      // Use LRU cache
      m_storage = new double[m_cacheSize * m_cacheSlots];
      m_keys = new long[m_cacheSize * m_cacheSlots];
    } 

    m_numInsts = m_data.numInstances();
  }

  /**
   * This method is overridden in subclasses to implement specific kernels.
   * 
   * @param id1
   *            the index of instance 1
   * @param id2
   *            the index of instance 2
   * @param inst1
   *            the instance 1 object
   * @return the dot product
   * @throws Exception
   */
  protected abstract double evaluate(int id1, int id2, Instance inst1)
    throws Exception;

  /**
   * Implements the abstract function of Kernel using the cache. This method
   * uses the evaluate() method to do the actual dot product.
   */
  public double eval(int id1, int id2, Instance inst1) throws Exception {
		
    double result = 0;
    long key = -1;
    int location = -1;

    // we can only cache if we know the indexes
    if (id1 >= 0) {

      // Use full cache?
      if (m_cacheSize == 0) {
	if (m_kernelMatrix == null) {
	  m_kernelMatrix = new double[m_data.numInstances()][];
	  for(int i = 0; i < m_data.numInstances(); i++) {
	    m_kernelMatrix[i] = new double[i + 1];
	    for(int j = 0; j <= i; j++) {
	      m_kernelEvals++;
	      m_kernelMatrix[i][j] = evaluate(i, j, m_data.instance(i));
	    }
	  }
	} 
	m_cacheHits++;
	result = (id1 > id2) ? m_kernelMatrix[id1][id2] : m_kernelMatrix[id2][id1];
	return result;
      }

      // Use LRU cache
      if (id1 > id2) {
	key = (id1 + ((long) id2 * m_numInsts));
      } else {
	key = (id2 + ((long) id1 * m_numInsts));
      }
      location = (int) (key % m_cacheSize) * m_cacheSlots;
      int loc = location;
      for (int i = 0; i < m_cacheSlots; i++) {
	long thiskey = m_keys[loc];
	if (thiskey == 0)
	  break; // empty slot, so break out of loop early
	if (thiskey == (key + 1)) {
	  m_cacheHits++;
	  // move entry to front of cache (LRU) by swapping
	  // only if it's not already at the front of cache
	  if (i > 0) {
	    double tmps = m_storage[loc];
	    m_storage[loc] = m_storage[location];
	    m_keys[loc] = m_keys[location];
	    m_storage[location] = tmps;
	    m_keys[location] = thiskey;
	    return tmps;
	  } else
	    return m_storage[loc];
	}
	loc++;
      }
    }

    result = evaluate(id1, id2, inst1);

    m_kernelEvals++;

    // store result in cache
    if (key != -1) {
      // move all cache slots forward one array index
      // to make room for the new entry
      System.arraycopy(m_keys, location, m_keys, location + 1,
		       m_cacheSlots - 1);
      System.arraycopy(m_storage, location, m_storage, location + 1,
		       m_cacheSlots - 1);
      m_storage[location] = result;
      m_keys[location] = (key + 1);
    }
    return result;
  }

  /**
   * Returns the number of time Eval has been called.
   * 
   * @return the number of kernel evaluation.
   */
  public int numEvals() {
    return m_kernelEvals;
  }

  /**
   * Returns the number of cache hits on dot products.
   * 
   * @return the number of cache hits.
   */
  public int numCacheHits() {
    return m_cacheHits;
  }

  /**
   * Frees the cache used by the kernel.
   */
  public void clean() {
    m_storage = null;
    m_keys = null;
    m_kernelMatrix = null;
  }

  /**
   * Calculates a dot product between two instances
   * 
   * @param inst1
   *            the first instance
   * @param inst2
   *            the second instance
   * @return the dot product of the two instances.
   * @exception Exception
   *                if an error occurs
   */
  protected final double dotProd(Instance inst1, Instance inst2)
    throws Exception {

    double result = 0;

    // we can do a fast dot product
    int n1 = inst1.numValues();
    int n2 = inst2.numValues();
    int classIndex = m_data.classIndex();
    for (int p1 = 0, p2 = 0; p1 < n1 && p2 < n2;) {
      int ind1 = inst1.index(p1);
      int ind2 = inst2.index(p2);
      if (ind1 == ind2) {
	if (ind1 != classIndex) {
	  result += inst1.valueSparse(p1) * inst2.valueSparse(p2);
	}
	p1++;
	p2++;
      } else if (ind1 > ind2) {
	p2++;
      } else {
	p1++;
      }
    }
    return (result);
  }

}
