package avuuna.player.controller;

import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;

import avuuna.player.exception.*;
import avuuna.player.model.*;
import avuuna.player.utils.*;
import avuuna.player.view.*;
import javazoom.jl.player.basic.*;

public class PlayerController implements Serializable, Observador {
	private static final long serialVersionUID = 232492703123683857L;

	private Player model;
	private GUIPlayer view;
	
	private boolean running;
    private final Thread progressThread;

	public PlayerController(BasicPlayer basicPlayer) {
		model = Player.getInstance(basicPlayer);
		view = new GUIPlayer();

		definaAcciones();
		
		running = false;
		progressThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    if (model.getActual() != null) {
                        view.playerPanel.progressBar.setValue((int) model.getProgressBytes());
                        try {
                        	view.playerPanel.progressBar.setString(Utils.formatTime(model.getProgressTime()) + " of " + Utils.formatTime(model.getActual().getDuration()));
                        } catch (Exception ex) {
                            Utils.display("Exception found -> " + ex.getMessage());
                        }
                    } else {
                    	view.playerPanel.progressBar.setValue(0);
                    	view.playerPanel.progressBar.setString("00:00 of 00:00");
                    }
                }
            }
        };
        progressThread.start();
	}

	private void definaAcciones() {
		view.openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));

		view.openItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				openItemActionPerformed(evt);
			}
		});
		
		view.playerPanel.playButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	switch (view.playerPanel.playButton.getText()) {
        		case GUIPanelPlayer.play:
        			if (!running && model.getActual() != null) {
        				playSong();
        			} else if (running) {
        				resumeSong();
        			}
        			break;
        		case GUIPanelPlayer.pause:
        			pauseSong();
        			break;
        		}
            }
        });
		
		view.playerPanel.stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	stopSong();
            }
        });
		
		view.playerPanel.nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                nextSong();
            }
        });
		
		view.playerPanel.previousButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	previousSong();
            }
        });
		
		view.playerPanel.volumeControl.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
            	setVolume();
            }
        });
		
		view.playlistPanel.songList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
            	if (evt.getClickCount() == 2) {// Doble clic
                    String selected = (String) view.playlistPanel.songList.getSelectedValue();
                    if (!selected.contains(GUIPlaylistPanel.ACTUAL)) {
                        openSong(selected);
                    }
                }
            }
        });
		
		view.playlistPanel.clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	clearPlaylist();
            }
        });
	}

	private void openItemActionPerformed(ActionEvent evt) {
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos MP3", "mp3");
		view.fileChooser.setFileFilter(filter);
		view.fileChooser.setMultiSelectionEnabled(true);
		int seleccion = view.fileChooser.showOpenDialog(view);
		if (seleccion == JFileChooser.APPROVE_OPTION) {
			File[] songs = view.fileChooser.getSelectedFiles();
			if (songs.length > 0) {
				for (File s : songs) {
					if (s.getAbsolutePath().endsWith(".mp3")) {
						try {
							Song song = new Song(s.getAbsolutePath());
							model.addSong(song);
							if (model.getActual() == null) {
								model.setActual(song);
								model.open(model.getActual());
							}
						} catch (BasicPlayerException | PlayerException ex) {
							Utils.log(PlayerException.ERROR_OPENING_SONG, ex);
//							JOptionPane.showMessageDialog(null, ex.getMessage(), PlayerException.ERROR_OPENING_SONG, JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
		}
	}

    private void playSong() {
        try {
            model.play();
        } catch (BasicPlayerException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), PlayerException.ERROR_PLAYING_SONG, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void pauseSong() {
        try {
        	model.pause();
        } catch (BasicPlayerException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), PlayerException.ERROR_PAUSING_SONG, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resumeSong() {
        try {
        	model.resume();
        } catch (BasicPlayerException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), PlayerException.ERROR_RESUMING_SONG, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void stopSong() {
        try {
        	model.stop();
        } catch (BasicPlayerException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), PlayerException.ERROR_STOPPING_SONG, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void nextSong() {
        try {
        	model.next();
        } catch (BasicPlayerException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), PlayerException.ERROR_NEXT_SONG, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void previousSong() {
        try {
        	model.previous();
        } catch (BasicPlayerException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), PlayerException.ERROR_PREV_SONG, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setVolume() {
        try {
        	model.getPlayer().setGain((double) view.playerPanel.volumeControl.getValue() / 100);
        } catch (BasicPlayerException ex) {
//            JOptionPane.showMessageDialog(null, ex.getMessage(), PlayerException.ERROR_VOLUME_SONG, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearPlaylist() {
        try {
            model.clearList();
        } catch (BasicPlayerException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), PlayerException.ERROR_CLEAR_LIST, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openSong(String selected) {
        try {
            Song song = model.getSong(selected);
            model.setActual(song);
            model.stop();
            model.open(song);
        } catch (BasicPlayerException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), PlayerException.ERROR_OPENING_SONG, JOptionPane.ERROR_MESSAGE);
        }
    }

	@Override
	public void update() {
		if (model.getActual() != null) {
            switch (model.getActualEvent()) {
                case BasicPlayerEvent.RESUMED:
                    view.playerPanel.playButton.setText(GUIPanelPlayer.pause);
                    break;
                case BasicPlayerEvent.PAUSED:
                	view.playerPanel.playButton.setText(GUIPanelPlayer.play);
                    break;
                case BasicPlayerEvent.PLAYING:
                	view.playerPanel.playButton.setText(GUIPanelPlayer.pause);
                    running = true;
                    break;
                case BasicPlayerEvent.STOPPED:
                	view.playerPanel.playButton.setText(GUIPanelPlayer.play);
                    running = false;
                    break;
                case BasicPlayerEvent.OPENED:
                	view.playerPanel.actualSong.setText(GUIPanelPlayer.cancionActual + model.getActual().getName());
                	view.playerPanel.progressBar.setMaximum((int) model.getActual().getBytesLength());
                	view.playerPanel.progressBar.setString("00:00 of " + Utils.formatTime(model.getActual().getDuration()));
                    if (!running && model.getActual() != null) {
                        playSong();
                    }
                    setVolume();
                    break;
            }
        } else {
        	view.playerPanel.actualSong.setText(GUIPanelPlayer.cancionActual);
        	view.playerPanel.progressBar.setMaximum(0);
        }
		
		view.playlistPanel.listModel.clear();
        for (Song song : model.getSongs()) {
            String resp = song.getName();
            if (model.getActual() != null && model.getActual().equals(song)) {
                resp = GUIPlaylistPanel.ACTUAL + resp;
            }
            view.playlistPanel.listModel.addElement(resp);
        }
	}

}
