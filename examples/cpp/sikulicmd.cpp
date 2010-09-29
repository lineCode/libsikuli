/*
 *  runocr.cpp
 *  sikuli
 *
 *  Created by Tom Yeh on 9/12/10.
 *  Copyright 2010 sikuli.org. All rights reserved.
 *
 */

#include "myocr.h"
#include "ocr.h"


int ocr_main(int argc, const char* argv[]){
   const char* filename = argv[1];
   OCRText text = OCR::recognize_screenshot(filename);

   char buf[200];
   sprintf(buf,"%s.ocr.txt", filename);
   text.save(buf);

   sprintf(buf,"%s.ocr.loc", filename);
   text.save_with_location(buf);
   
   return 1;
}



int ocrdir_main(int argc, const char* argv[]){
   
   
   const char* inputdir = argv[1];
   int n = atoi(argv[2]);
   
   
   
   for (int i=0;i<n;++i){
   
      char buf[200];
 
      sprintf(buf,"%s/screen%d.png", inputdir, i);
      cout << buf;
      OCRText text = OCR::recognize_screenshot(buf);      
      
      sprintf(buf,"%s/screen%d.png.ocr.txt", inputdir, i);
      text.save(buf);
      
      sprintf(buf,"%s/screen%d.png.ocr.loc", inputdir, i);
      text.save_with_location(buf);
   
   }
   
   return 1;
}



#include "vision.h"
using namespace sikuli;
int match_main(int argc, const char* argv[]){
   
   ScreenImage screen(argv[1]);
   Pattern ptn(argv[2]);
   
   vector<FindResult> results;
   results = Vision::find(screen, ptn.all());
   
   for (vector<FindResult>::iterator r = results.begin(); 
        r != results.end(); ++r){
      
      FindResult& result = *r;
      
      cout << result.x << " " << result.y << endl;
      
   }
   
   return 1;
}

#include "cvgui.h"
int index_main(int argc, const char* argv[]){
   if (argc < 4)
      return -1;
   
   Database db;
   
   const char* inputdir = argv[1];
   int n = atoi(argv[2]);
   const char* output = argv[3];
   
   char filename[200];
   
   for (int i=0;i<n;++i){
      sprintf(filename, "%s/screen%d.png", inputdir, i);
      db.insert_file(filename, i);
   }
      
   ofstream out(output, ios::binary);
   db.write(out);
   out.close();
   
   return 1;
}


int query_main(int argc, const char* argv[]){
   if (argc < 3)
      return -1;
   
   Database db;

   const char* index_filename = argv[1];   
   const char* query_image_filename = argv[2];
   
   ifstream in(index_filename, ios::binary);
   db.read(in);
   in.close();
 
   
   vector<ImageRecord> results = db.find(query_image_filename);
   
   for (vector<ImageRecord>::iterator r = results.begin(); 
        r != results.end(); ++r){
      
      ImageRecord& record = *r;
      
      cout << "ui" << record.id << " ";
      
   }
   
   return 1;
}


int main(int argc, const char* argv[]){
   
   if (argc < 2)
      return 0;
   
   const char* command = argv[1];
   
   if (strcmp(command, "OCR") == 0)
      return ocr_main(argc-1, argv+1);
   
   else if (strcmp(command, "OCRDIR") == 0)
      return ocrdir_main(argc-1, argv+1);   

   else if (strcmp(command, "MATCH") == 0)
      return match_main(argc-1, argv+1);

   else if (strcmp(command, "INDEX") == 0)
      return index_main(argc-1, argv+1);
   
   else if (strcmp(command, "QUERY") == 0)
      return query_main(argc-1, argv+1);
   
   return 0;
}