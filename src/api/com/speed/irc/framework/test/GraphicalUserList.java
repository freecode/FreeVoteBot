package com.speed.irc.framework.test;

import com.speed.irc.framework.Bot;
import com.speed.irc.types.Channel;
import com.speed.irc.types.ChannelUser;

import javax.swing.*;

/**
 * Displays a visual list of all the users in the channel, and associated
 * properties.
 * <p/>
 * This file is part of Speed's IRC API.
 * <p/>
 * Speed's IRC API is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * <p/>
 * Speed's IRC API is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with Speed's IRC API. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Shivam Mistry
 */
public class GraphicalUserList extends JFrame implements Runnable {

    private Channel mainChannel;
    private JList<ChannelUser> list;
    private ListModel<ChannelUser> model;

    public static void main(String[] args) {
        new GraphicalUserList();
    }

    public GraphicalUserList() {
        Bot bot = new Bot("irc.rizon.net", 6697, true) {

            public void onStart() {
                mainChannel = new Channel("#freecode", getServer());
                getServer().setReadDebug(true);
            }

            public Channel[] getChannels() {
                return new Channel[]{mainChannel};
            }

            public String getNick() {
                return "UserLister";
            }

        };
        setSize(200, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("User List");
        model = new AbstractListModel<ChannelUser>() {

            @Override
            public int getSize() {
                return mainChannel.getUsers().size();
            }

            @Override
            public ChannelUser getElementAt(int index) {
                return mainChannel.getSortedUsers()[index];
            }
        };
        list = new JList<ChannelUser>(model);
        list.setFixedCellHeight(15);
        JScrollPane pane = new JScrollPane(list,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(pane);
        setVisible(true);
        new Thread(this).start();
    }

    public void run() {
        while (isVisible() && mainChannel.isJoined()) {
            //okay we should REALLY be using API listeners for this but w.e
            setTitle("User Lister " + mainChannel.getName() + " " + mainChannel.getModeList().parse());
            repaint();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
