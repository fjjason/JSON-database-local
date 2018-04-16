
import java.io.BufferedReader;
    import java.io.BufferedWriter;
    import java.io.File;
    import java.io.FileReader;
    import java.io.FileWriter;
    import java.io.IOException;
    import java.util.Map.Entry;
    import java.util.Scanner;
    import java.util.Set;

    import com.google.gson.JsonArray;
    import com.google.gson.JsonElement;
    import com.google.gson.JsonObject;
    import com.google.gson.JsonParser;

    public class json_db {
        public static void main(String args[]) throws Exception {
            //open file, we will use it like a SQL database
            
            File databaseFile = new File("c:\\Jason\\database.txt");
            // if file doesnt exists, then create it
            if (!databaseFile.exists()) {
                databaseFile.createNewFile();
            }
            //open scanner loop through each line of file
            Scanner input = new Scanner(System.in);

            while (input.hasNextLine()) {
                //corner cases
                String str = input.nextLine();
                InputCommand inputCommand = new InputCommand(str);
                if (inputCommand.getCommand() == null || inputCommand.getCommand().length() < 3) {
                    System.err.println("Invalid input"+inputCommand.getCommand());
                    continue;
                }
                JsonObject inputJsonObject = toJsonObject(inputCommand.getJsonDocument());
                if (inputJsonObject == null) {
                    System.err.println("Invalid Json");
                    continue;
                }
                //if add/get/delete, then perform those tasks, pass parameters, the input line , the command, database file
                if (inputCommand.getCommand().equals("add")) {
                    processAdd(inputCommand.getJsonDocument(), databaseFile);
                } else if (inputCommand.getCommand().equals("get")) {
                    processGet(inputJsonObject, databaseFile);
                } else if (inputCommand.getCommand().equals("delete")) {
                    processDelete(inputJsonObject, databaseFile);
                } else {
                    System.err.println("Invalid command, we supports add/get/delete");
                }
            }
            input.close();
        }
        
        /*if you look at the input, get/add/delete is before space " ". Everything before space is add/get/delete, everything 
        after than it's JSON objects. I created a class to make life simpler
        */
        protected static class InputCommand {
            private String command;
            
            private String jsonDocument;
            public InputCommand(String input) {
                int i = input.indexOf(" ");
                if (i > 0) {
                    command = input.substring(0, i );
                    jsonDocument = input.substring(i + 1);
                } else {
                    command = null;
                    jsonDocument = null;
                }
            }

            public String getCommand() {
                return command;
            }

            public void setCommand(String command) {
                this.command = command;
            }

            public String getJsonDocument() {
                return jsonDocument;
            }

            public void setJsonDocument(String jsonDocument) {
                this.jsonDocument = jsonDocument;
            }

        }
        //parse string to object
        private static JsonObject toJsonObject(String json) {
            try {
                return new JsonParser().parse(json).getAsJsonObject();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        /*compare JSON from database and JSON query. 
        */
    	private static boolean matched(JsonObject originalDocument, JsonObject queryDocument) {
    		Set<Entry<String, JsonElement>> ents = queryDocument.entrySet();
    		//if query is null then not matched
            if (ents.size() == 0) {
    			return false;
    		}
    		boolean matched = true;
            //for each item in query statement
    		for (Entry<String, JsonElement> ent : ents) {
    			//System.out.println("Checking " + ent.getKey() + " with value " + ent.getValue().toString());
    			JsonElement sourceElement = originalDocument.get(ent.getKey());
    			if (sourceElement != null) {
                    
    				if (ent.getValue().isJsonNull()) {
    					continue;
    				} else if (ent.getValue().isJsonArray()) {    
    					//System.out.println("Checking Array");
    					if (sourceElement.isJsonArray()) {
                            //check if a nested JSON array is empty
    						JsonArray attrArray = ent.getValue().getAsJsonArray();
    						if (attrArray.size() == 0) {
    							matched = false;
    							break;
    						}
    						JsonArray sourceArray = sourceElement.getAsJsonArray();
                            //loop through each element of nested JSON query array with source/database array 
    						for (JsonElement ae : attrArray) {
    							boolean found = false;
    							for (JsonElement se : sourceArray) {
    								if (se.getAsString().equals(ae.getAsString())) {
    									found = true;
    									break;
    								}
    							}
    							if (!found) {
    								matched = false;
    								break;

    							}
    						}
    					} else {//if 
    						//System.out.println("value type mismatch");
    						matched = false;
    						break;
    					}
    				} else if (ent.getValue().toString().equals(sourceElement.toString())) {
    					// Assuming no nested Json attr to search
    					//System.out.println("value matched woohoo");
    					matched = true;
    				} else if (ent.getValue().isJsonObject() && ent.getValue().isJsonObject()) {
    					matched = contains(sourceElement.getAsJsonObject(), ent.getValue().getAsJsonObject());
    				} else {
    					//System.out.println("value not equal");
    					matched = false;
    					break;
    				}
    			} else {
    				//System.out.println("your get() is empty sir");
    				matched = false;
    				break;
    			}
    		}
    		return matched;
    	}

    	private static boolean contains(JsonObject originalDocument, JsonObject queryDocument) {
    		Set<Entry<String, JsonElement>> ents = queryDocument.entrySet();
    		if (ents.size() == 0) {
    			return false;
    		}
    		boolean matched = true;
    		for (Entry<String, JsonElement> ent : ents) {
    			JsonElement sourceElement = originalDocument.get(ent.getKey());
    			if (sourceElement != null) {
    				if (ent.getValue().isJsonNull()) {
    					continue;
    				} else if (ent.getValue().toString().equals(sourceElement.toString())) {
    					matched = true;
    				} else if (ent.getValue().isJsonObject()) {
    					matched = contains(sourceElement.getAsJsonObject(), ent.getValue().getAsJsonObject());
    				} else {
    					matched = false;
    					break;
    				}
    			} else {
    				matched = false;
    				break;
    			}
    		}
    		return matched;
    	}
        /*Add(): parse string into JSON objects, put JSON objects in database.txt which acts as a 
        database, write current line of STDIN input to file, close file 
            */
        private static boolean processAdd(String originalJson, File databaseFile) {
            BufferedWriter bw = null;
            FileWriter fw = null;

            try {
                fw = new FileWriter(databaseFile, true); // Append mode
                bw = new BufferedWriter(fw);
                bw.write(originalJson);
                bw.newLine();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bw != null) {
                        bw.close();
                    }
                    if (fw != null) {
                        fw.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        }
        
        // If open database, loop through every line, parse line into JSON object. If object and query matches, then output. 
        private static boolean processGet(JsonObject queryDocument, File databaseFile) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(databaseFile));
                String line;
                while ((line = br.readLine()) != null) {
                    JsonObject originalDocument = toJsonObject(line);
                    if (originalDocument != null) {
                        if (matched(originalDocument, queryDocument)) {
                            System.out.println(line);
                        }
                    }
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        }
        /*create temp file, turn each line into JSON. Loop through source/database file, output every element
        that does not match the query to this temp file. Close temp file, put back to original file, this way the
        rest of the data is unharmed :DDD
        */
        private static boolean processDelete(JsonObject queryDocument, File databaseFile) {
            BufferedWriter bw = null;
            FileWriter fw = null;
            BufferedReader br = null;
            File databaseFileNew = new File(databaseFile.getAbsolutePath() + ".log");
            try {
                fw = new FileWriter(databaseFileNew);
                bw = new BufferedWriter(fw);
                br = new BufferedReader(new FileReader(databaseFile));
                String line;
                while ((line = br.readLine()) != null) {
                    JsonObject originalDocument = toJsonObject(line);
                    if (originalDocument != null) {
                        if (!matched(originalDocument, queryDocument)) {
                            bw.write(line);
                            bw.newLine();
                        }
                    }
                }
                br.close();
                bw.close();
                fw.close();
                databaseFile.delete();
                databaseFileNew.renameTo(databaseFile);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                    if (bw != null) {
                        bw.close();
                    }
                    if (fw != null) {
                        fw.close();
                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        }
    }
