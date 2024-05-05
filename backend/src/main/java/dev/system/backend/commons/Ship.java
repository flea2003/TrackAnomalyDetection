package dev.system.backend.commons;
import jakarta.persistence.*;
import java.util.List;

@Entity
public class Ship {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long shipID;

    @OneToMany(mappedBy = "ship", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AIS> signalHistory;

    @OneToMany(mappedBy = "ship", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnomalyInformation> anomalyInformationHistory;

    public Ship(){

    }

    public Ship(long shipID, List<AIS> signalHistory, List<AnomalyInformation> anomalyInformationHistory){
        this.shipID = shipID;
        this.signalHistory = signalHistory;
        this.anomalyInformationHistory = anomalyInformationHistory;
    }

    public long getShipID() {
        return shipID;
    }

    public List<AIS> getWindowedSignalHistory(int windowSize) throws IndexOutOfBoundsException {
        if(windowSize>signalHistory.size()){
            throw new IndexOutOfBoundsException("Invalid window size");
        }
        return signalHistory.subList(signalHistory.size()-1-windowSize,signalHistory.size()-1);
    }

    public List<AIS> getSignalHistory() {
        return signalHistory;
    }

    public AIS getLastSignal(){
        return signalHistory.get(signalHistory.size()-1);
    }

    public List<AnomalyInformation> getAnomalyScoreHistory() {
        return anomalyInformationHistory;
    }


    public List<AnomalyInformation> getWindowedAnomalyScoreHistory(int windowSize) throws IndexOutOfBoundsException{
        if(windowSize> anomalyInformationHistory.size()){
            throw new IndexOutOfBoundsException("Invalid window size");
        }
        return anomalyInformationHistory.subList(anomalyInformationHistory.size()-1-windowSize, anomalyInformationHistory.size()-1);
    }

    public AnomalyInformation getLastAnomalyScore(){
        return anomalyInformationHistory.get(anomalyInformationHistory.size()-1);
    }
}
