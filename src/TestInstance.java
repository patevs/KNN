
public class TestInstance extends Instance{

	private String predictedLabel = "";
	
	public TestInstance(float[] attr, String label) {
		super(attr, label);
	}

	public void setPredictedLabel(String label) {
		this.predictedLabel = label;
	}
	public String getPredictedLabel(){
		return this.predictedLabel;
	}

}
