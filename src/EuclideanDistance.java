
public class EuclideanDistance implements Distance{

	public float getDistance(Instance e1, Instance e2) {
		
		if(e1.attributes.length != e2.attributes.length){
			System.err.println("Instances must have the same number of attributes");
			return -1;
		}
		
		double sum = 0;
		int numAttr = e1.attributes.length;
		for(int i=0;i<numAttr;i++){
			sum += Math.pow((e1.attributes[i] - e2.attributes[i]), 2);
		}
		return (float) Math.sqrt(sum);
	}

}
