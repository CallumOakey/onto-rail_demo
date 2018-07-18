package mesea.svg;

import mesea.model.TrackMaintenance;
import odase.FastConfig;
import odase.KnowledgeBase;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class SvgWriter {


    public static void main(String[] args) throws IOException{
        if(args.length != 2){
            System.out.println(Arrays.asList(args));
            System.out.println("Expected arguments: [outputFile] [gradingScale]");
            return;
        }

        KnowledgeBase kb = new KnowledgeBase("catalog.xml", "", Arrays.asList(
                "http://www.mesea.fr/onto-maintenance/ontologies/main/degradationPoints.swd",
                "http://www.mesea.fr/onto-maintenance/ontologies/main/mockTest.owl"),
                "rdfstore:rdftrie",
                new FastConfig());
        SvgWriter svgWriter = new SvgWriter(kb);
        int scale = Integer.parseInt(args[1]);
        svgWriter.makeSvg(scale);

        String filePath = args[0];
        svgWriter.save(filePath);
    }

    private int id = 0;
    private TrackMaintenance tm;
    private String retVal;
    private List<Point> statusPoints = new ArrayList<>();
    private final double HORIZONTAL_OFFSET = 14;
    private final double GRAPH_HIGHT = 16;
    private final Integer SCALE_COEFFICIENT = 20;
    private final Integer GRAPH_LENGTH = 180;

    SvgWriter(KnowledgeBase kb){
        tm = new TrackMaintenance(kb);
    }

    void makeSvg(int scale){
        scale = scale / SCALE_COEFFICIENT;

        addSvgStart();

        Integer j = 0;
        for(TrackMaintenance.Track track : tm.allTrack()){
            for(TrackMaintenance.Rail rail : track.getComposedOfRail()){
                double y = (23.5 + j * 20);
                double yMiddle = y + (GRAPH_HIGHT / 2);

                addGraphStart(y);

                addVerticalScale(y);
                addHorizontalScale(scale, yMiddle);

                Collection<TrackMaintenance.Measurement> measurements = rail.getHasMeasurement();
                TrackMaintenance.Measurement mesure = new TrackMaintenance.Measurement(tm.getModel());
                for(TrackMaintenance.Measurement m : measurements){
                    if(m.getPrecededByMeasurement() == null){
                        mesure = m;
                        break;
                    }
                }

                addPathStart();

                double previousValue = 0;
                for(Integer i = 0 ; i < measurements.size(); i++){
                    double xPoint = i == 0 ? HORIZONTAL_OFFSET : 1;
                    double yPoint = i == 0 ? yMiddle : 0;
                    previousValue = addGraphPoint(mesure, previousValue, xPoint, yPoint);

                    if (mesure != null && mesure.isAlertPoint()){
                        statusPoints.add(new AlertPoint(i + HORIZONTAL_OFFSET, yMiddle - mesure.getDeviationValue(), mesure.getDeviationValue()));
                    }
                    if (mesure != null && mesure.isInterventionPoint()){
                        statusPoints.add(new InterventionPoint(i + HORIZONTAL_OFFSET, yMiddle - mesure.getDeviationValue(), mesure.getDeviationValue()));
                    }
                    if (mesure != null && mesure.isSlowdownPoint()){
                        statusPoints.add(new SlowdownPoint(i + HORIZONTAL_OFFSET, yMiddle - mesure.getDeviationValue(), mesure.getDeviationValue()));
                    }

                    mesure = mesure.getFollowedByMeasurement();
                }

                addGraphEnd();

                j++;
            }
        }

        addStatusSignals();

        retVal += "</svg>";
    }

    int newId(){
        id++;
        return id;
    }

    void addVerticalScale(double y){
        for(int i = 0; i < GRAPH_HIGHT + 1 ; i++){
            addPathStart();
            retVal += HORIZONTAL_OFFSET + "," + (y + i) + " ";
            retVal += (- 0.5) + "," + 0 + "\n";
            addGraphEnd();
        }
    }

    void addHorizontalScale(int scale, double yMiddle){
        for(int i = 1; i < (GRAPH_LENGTH / scale) + 1; i++){
            addPathStart();
            retVal += (HORIZONTAL_OFFSET + scale * i) + "," + yMiddle + " ";
            retVal += 0 + "," + (- 0.5) + "\n";
            addGraphEnd();
        }
    }

    void addStatusSignals(){
        int j = 0;
        for(Point point : statusPoints){
            retVal += "  <path\n" +
                    "     style=\"fill:none;stroke:#" + point.getColor() + ";stroke-width:0.20032115px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1\"\n" +
                    "     d=\"m " + point.getX() + "," + point.getY() + " " + 0 + "," + point.getYOrigin() + "\"\n" +
                    "     id=\"path" + newId() + "\" />\n";
            j++;
        }
    }

    void addSvgStart(){
        retVal = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "\n" +
                "<svg\n" +
                "   xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n" +
                "   xmlns:cc=\"http://creativecommons.org/ns#\"\n" +
                "   xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
                "   xmlns:svg=\"http://www.w3.org/2000/svg\"\n" +
                "   xmlns=\"http://www.w3.org/2000/svg\"\n" +
                "   xmlns:sodipodi=\"http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd\"\n" +
                "   width=\"210mm\"\n" +
                "   height=\"297mm\"\n" +
                "   viewBox=\"0 0 210 297\"\n" +
                "   version=\"1.1\"\n" +
                "   id=\"svg8\"\n" +
                "   sodipodi:docname=\"drawingTemplate.svg\">\n" +
                "  <defs\n" +
                "     id=\"defs2\" />\n" +
                "  <sodipodi:namedview\n" +
                "     id=\"base\"\n" +
                "     pagecolor=\"#ffffff\"\n" +
                "     bordercolor=\"#666666\"\n" +
                "     borderopacity=\"1.0\">\n" +
                "  </sodipodi:namedview>\n" +
                "  <metadata\n" +
                "     id=\"metadata5\">\n" +
                "    <rdf:RDF>\n" +
                "      <cc:Work\n" +
                "         rdf:about=\"\">\n" +
                "        <dc:format>image/svg+xml</dc:format>\n" +
                "        <dc:type\n" +
                "           rdf:resource=\"http://purl.org/dc/dcmitype/StillImage\" />\n" +
                "        <dc:title />\n" +
                "      </cc:Work>\n" +
                "    </rdf:RDF>\n" +
                "  </metadata>\n";
    }

    void addPathStart(){
        retVal +="  <path\n" +
                "     style=\"fill:none;stroke:#000000;stroke-width:0.265px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1\"\n" +
                "     d=\"m ";
    }

    double addGraphPoint(TrackMaintenance.Measurement mesure, double previousValue, double x, double y){
        double currentValue = mesure.getDeviationValue();
        double verticalValue = previousValue - currentValue;

        retVal += " " + x + "," + (y + verticalValue);

        return currentValue;
    }

    void addGraphStart(double y){
        retVal += "  <rect\n" +
                "     style=\"fill-rule:evenodd;stroke-width:0.265;fill:#e6e6e6\"\n" +
                "     id=\"rect" + newId() + "\"\n" +
                "     width=\"" + GRAPH_LENGTH + "\"\n" +
                "     height=\"" + GRAPH_HIGHT + "\"\n" +
                "     x=\"" + HORIZONTAL_OFFSET +
                "\"\n" +
                "     y=\"" + y + "\" />\n" +
                "  <path\n" +
                "     style=\"fill:none;stroke:#000000;stroke-width:0.20032115px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1\"\n" +
                "     d=\"m " + HORIZONTAL_OFFSET +
                "," + y + " v "+ GRAPH_HIGHT + "\"\n" +
                "     id=\"path" + newId() + "\" />\n" +
                "  <path\n" +
                "     style=\"fill:none;stroke:#000000;stroke-width:0.27630758px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1\"\n" +
                "     d=\"M " + HORIZONTAL_OFFSET +
                "," + (y + GRAPH_HIGHT / 2) + " h " + GRAPH_LENGTH + "\"\n" +
                "     id=\"path" + newId() + "\" />\n";
    }

    void addGraphEnd(){
        retVal += "\"\n" +
                "     id=\"path" + newId() + "\" />\n";
    }

    private void save(String filePath) throws IOException{
        PrintWriter writer = new PrintWriter(filePath, "UTF-8");
        writer.print(retVal);
        writer.close();
    }
}
