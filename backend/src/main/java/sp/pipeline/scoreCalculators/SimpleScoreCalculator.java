//package sp.pipeline.scoreCalculators;
//
//import org.apache.flink.streaming.api.datastream.DataStream;
//import org.apache.flink.streaming.api.datastream.KeyedStream;
//import sp.dtos.AISSignal;
//import sp.dtos.AnomalyInformation;
//import sp.pipeline.scoreCalculators.components.SignalStatefulMapFunction;
//import sp.pipeline.scoreCalculators.components.SpeedStatefulMapFunction;
//import sp.pipeline.scoreCalculators.components.TurningStatefulMapFunction;
//
//public class SimpleScoreCalculator implements ScoreCalculationStategy{
//    @Override
//    public DataStream<AnomalyInformation> setupFlinkAnomalyScoreCalculationPart(DataStream<AISSignal> source) {
//        KeyedStream<AISSignal, String> keyedStream = source.keyBy(AISSignal::getShipHash);
//
//        DataStream<AnomalyInformation>signalUpdates = keyedStream.map(new SignalStatefulMapFunction());
//        DataStream<AnomalyInformation>speedUpdates = keyedStream.map(new SpeedStatefulMapFunction());
//        DataStream<AnomalyInformation>turningUpdates = keyedStream.map(new TurningStatefulMapFunction());
//
//        DataStream<AnomalyInformation>mergedStream = signalUpdates.union(speedUpdates, turningUpdates);
//        mergedStream.keyBy(AnomalyInformation::getShipHash).reduce((update1, update2) -> {
//
//            if(update1)
//
//            if(update1.getCorrespondingTimestamp() != update2.getCorrespondingTimestamp()) {
//                AnomalyInformation aggregatedUpdate = new AnomalyInformation();
//                aggregatedUpdate.setScore(update1.getScore() + update2.getScore());
//                aggregatedUpdate.setExplanation(update1.getExplanation() + ',' + update2.getExplanation());
//            }
//            else{
//
//            }
//        });
//
//        return mergedStream;
//    }
//}
