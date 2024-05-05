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
    private List<AnomalyScore> anomalyScoreHistory;

    public Ship(){

    }

    public Ship(long shipID, List<AIS> signalHistory, List<AnomalyScore> anomalyScoreHistory){
        this.shipID = shipID;
        this.signalHistory = signalHistory;
        this.anomalyScoreHistory = anomalyScoreHistory;
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

    public List<AnomalyScore> getAnomalyScoreHistory() {
        return anomalyScoreHistory;
    }


    public List<AnomalyScore> getWindowedAnomalyScoreHistory(int windowSize) throws IndexOutOfBoundsException{
        if(windowSize>anomalyScoreHistory.size()){
            throw new IndexOutOfBoundsException("Invalid window size");
        }
        return anomalyScoreHistory.subList(anomalyScoreHistory.size()-1-windowSize, anomalyScoreHistory.size()-1);
    }

    public AnomalyScore getLastAnomalyScore(){
        return anomalyScoreHistory.get(anomalyScoreHistory.size()-1);
    }
}
