package kz.mobdev.mapview.data;

import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

import kz.mobdev.mapview.library.models.Marker;

/**
 * TestData
 *
 * @author: onlylemi
 */
public final class DataForBeacons {

    private DataForBeacons() {
    }

    public static List<PointF> getNodesList() {
        List<PointF> nodes = new ArrayList<>();
        //0
        nodes.add(new PointF(0, 706));
        //1
        nodes.add(new PointF(335, 117));
        //2
        nodes.add(new PointF(117, 777));
        //3
        nodes.add(new PointF(122, 888));
        //4
        nodes.add(new PointF(170, 928));
        //5
        nodes.add(new PointF(122, 1080));
        //6
        nodes.add(new PointF(175, 1053));
        //7
        nodes.add(new PointF(250, 1081));
        //8
        nodes.add(new PointF(369, 1122));
        //9
        nodes.add(new PointF(483, 1118));
        //10
        nodes.add(new PointF(807, 1119));
        //11
        nodes.add(new PointF(808, 823));
        //12
        nodes.add(new PointF(819, 663));
        //13
        nodes.add(new PointF(484, 829));
        //14
        nodes.add(new PointF(266, 829));
        //15
        nodes.add(new PointF(153, 713));
        //16
        nodes.add(new PointF(480, 678));
        //17
        nodes.add(new PointF(486, 504));
        //18
        nodes.add(new PointF(807, 505));
        //19
        nodes.add(new PointF(363, 469));
        //20
        nodes.add(new PointF(359, 435));
        //21
        nodes.add(new PointF(512, 314));
        //22
        nodes.add(new PointF(408, 254));
        //23
        nodes.add(new PointF(358, 329));
        //24
        nodes.add(new PointF(790, 323));
        //25
        nodes.add(new PointF(798, 202));
        //26
        nodes.add(new PointF(651, 220));
        //27
        nodes.add(new PointF(631, 35));
        //28
        nodes.add(new PointF(659, 656));

        return nodes;
    }

    public static List<PointF> getNodesContactList() {
        List<PointF> nodesContact = new ArrayList<PointF>();

        nodesContact.add(new PointF(0, 2));
        nodesContact.add(new PointF(0, 15));
        nodesContact.add(new PointF(2, 3));
        nodesContact.add(new PointF(3, 4));
        nodesContact.add(new PointF(2, 4));
        nodesContact.add(new PointF(2, 14));
        nodesContact.add(new PointF(2, 15));
        nodesContact.add(new PointF(3, 5));
        nodesContact.add(new PointF(3, 6));
        nodesContact.add(new PointF(4, 5));
        nodesContact.add(new PointF(4, 6));
        nodesContact.add(new PointF(4, 14));
        nodesContact.add(new PointF(5, 6));
        nodesContact.add(new PointF(6, 7));
        nodesContact.add(new PointF(7, 8));
        nodesContact.add(new PointF(8, 9));
        nodesContact.add(new PointF(9, 10));
        nodesContact.add(new PointF(9, 13));
        nodesContact.add(new PointF(11, 12));
        nodesContact.add(new PointF(11, 13));
        nodesContact.add(new PointF(13, 14));
        nodesContact.add(new PointF(13, 15));
        nodesContact.add(new PointF(13, 16));
        nodesContact.add(new PointF(14, 15));
        nodesContact.add(new PointF(14, 16));
        nodesContact.add(new PointF(15, 16));
        nodesContact.add(new PointF(16, 17));
        nodesContact.add(new PointF(16, 28));
        nodesContact.add(new PointF(17, 18));
        nodesContact.add(new PointF(17, 19));
        nodesContact.add(new PointF(17, 21));
        nodesContact.add(new PointF(19, 20));
        nodesContact.add(new PointF(21, 22));
        nodesContact.add(new PointF(21, 26));
        nodesContact.add(new PointF(21, 24));
        nodesContact.add(new PointF(22, 23));
        nodesContact.add(new PointF(22, 1));
        nodesContact.add(new PointF(22, 26));
        nodesContact.add(new PointF(25, 26));
        nodesContact.add(new PointF(26, 27));

        return nodesContact;
    }

    public static List<Marker> getMarks() {
        List<Marker> marks = new ArrayList<>();


        marks.add(Marker.newBuilder().setPosition(79, 812).build());
        marks.add(Marker.newBuilder().setPosition(89, 869).build());
        marks.add(Marker.newBuilder().setPosition(89, 927).build());
        marks.add(Marker.newBuilder().setPosition(89, 1056).build());
        marks.add(Marker.newBuilder().setPosition(253, 939).build());
        marks.add(Marker.newBuilder().setPosition(253, 1015).build());
        marks.add(Marker.newBuilder().setPosition(169, 1110).build());
        marks.add(Marker.newBuilder().setPosition(250, 1140).build());
        marks.add(Marker.newBuilder().setPosition(312, 1145).build());
        marks.add(Marker.newBuilder().setPosition(385, 920).build());
        marks.add(Marker.newBuilder().setPosition(385, 1035).build());
        marks.add(Marker.newBuilder().setPosition(582, 920).build());
        marks.add(Marker.newBuilder().setPosition(726, 920).build());

        marks.add(Marker.newBuilder().setPosition(582, 1035).build());
        marks.add(Marker.newBuilder().setPosition(726, 1035).build());
        marks.add(Marker.newBuilder().setPosition(740, 718).build());
        marks.add(Marker.newBuilder().setPosition(596, 698).build());
        marks.add(Marker.newBuilder().setPosition(316, 658).build());

        marks.add(Marker.newBuilder().setPosition(141, 670).build());

        marks.add(Marker.newBuilder().setPosition(376, 504).build());

        marks.add(Marker.newBuilder().setPosition(403, 388).build());
        marks.add(Marker.newBuilder().setPosition(745, 416).build());
        marks.add(Marker.newBuilder().setPosition(627, 416).build());
        marks.add(Marker.newBuilder().setPosition(779, 278).build());
        marks.add(Marker.newBuilder().setPosition(364, 255).build());
        marks.add(Marker.newBuilder().setPosition(408, 116).build());
        marks.add(Marker.newBuilder().setPosition(554, 128).build());
        marks.add(Marker.newBuilder().setPosition(740, 116).build());
        marks.add(Marker.newBuilder().setPosition(196, 674).build());
        //First position
        marks.add(Marker.newBuilder().setPosition(0, 706).build());


        return marks;
    }

    public static List<String> getMarksName() {
        List<String> marksName = new ArrayList<>();
        for (int i = 0; i < getMarks().size(); i++) {
            marksName.add("Shop " + (i + 1));
        }
        return marksName;
    }
}


