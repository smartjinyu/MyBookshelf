package com.smartjinyu.mybookshelf.support;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.opencsv.CSVWriter;
import com.smartjinyu.mybookshelf.R;
import com.smartjinyu.mybookshelf.model.BookLab;
import com.smartjinyu.mybookshelf.model.BookShelfLab;
import com.smartjinyu.mybookshelf.model.LabelLab;
import com.smartjinyu.mybookshelf.model.bean.Book;
import com.smartjinyu.mybookshelf.model.bean.BookShelf;
import com.smartjinyu.mybookshelf.model.bean.Label;
import com.smartjinyu.mybookshelf.util.AnswersUtil;
import com.smartjinyu.mybookshelf.util.SharedPrefUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Integer[] Items to export, which is corresponding to the order of items in string-array
 */
public class ExportCSVTask extends AsyncTask<Integer[], Void, Boolean> {
    private static final String TAG = "ExportCSVTask";
    private MaterialDialog mDialog;
    private String mCsvName;
    private Context mContext;

    public ExportCSVTask(Context context, String csvName) {
        this.mContext = context;
        this.mCsvName = csvName;
    }

    @Override
    protected void onPreExecute() {
        mDialog = new MaterialDialog.Builder(mContext)
                .title(R.string.export_progress_dialog_title).content(R.string.export_progress_dialog_content)
                .progress(true, 0).progressIndeterminateStyle(false)
                .canceledOnTouchOutside(false)
                .show();
    }

    @Override
    protected Boolean doInBackground(Integer[]... selectedItems) {
        try (FileOutputStream outputStream = new FileOutputStream(mCsvName)) {
            List<Book> mBooks = BookLab.get(mContext).getBooks();
            // sort Books
            int sortMethod = SharedPrefUtil.getInstance().getInt("SORT_METHOD", 0);
            Comparator<Book> comparator;
            switch (sortMethod) {
                case 0:
                    comparator = new Book.titleComparator();
                    break;
                case 1:
                    comparator = new Book.authorComparator();
                    break;
                case 2:
                    comparator = new Book.publisherComparator();
                    break;
                case 3:
                    comparator = new Book.pubtimeComparator();
                    break;
                default:
                    comparator = new Book.titleComparator();
            }
            Collections.sort(mBooks, comparator);

            int[] items = new int[11];
            for (int i = 0; i < selectedItems[0].length; i++) {
                items[selectedItems[0][i]] = 1;
            }
            // items is like [1,0,0,0,0,0,1,1,1,1,1], if one item needs to export, item[i] is 1
            outputStream.write(0xef);
            outputStream.write(0xbb);
            outputStream.write(0xbf);
            // use utf-8 with BOM to avoid messy code in Chinese while using MS Excel
            CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(outputStream));
            // write the title bar
            List<String> titleBar = new ArrayList<>();
            titleBar.add(mContext.getString(R.string.export_csv_file_order));
            for (int i = 0; i < 11; i++) {
                if (items[i] == 1) {
                    titleBar.add(mContext.getResources().getStringArray(R.array.export_csv_dialog_list)[i]);
                }
            }
            csvWriter.writeNext(titleBar.toArray(new String[0]));

            for (int i = 1; i <= mBooks.size(); i++) {
                List<String> entry = new ArrayList<>();
                Book mBook = mBooks.get(i - 1);
                entry.add(Integer.toString(i));
                if (items[0] == 1) {
                    // title
                    entry.add(mBook.getTitle());
                }
                if (items[1] == 1) {
                    // authors
                    String authors = mBook.getFormatAuthor();
                    if (authors != null) {
                        entry.add(authors);
                    } else {
                        entry.add("");
                    }
                }
                if (items[2] == 1) {
                    // translators
                    String translators = mBook.getFormatTranslator();
                    if (translators != null) {
                        entry.add(translators);
                    } else {
                        entry.add("");
                    }
                }
                if (items[3] == 1) {
                    // publisher
                    entry.add(mBook.getPublisher());
                }
                if (items[4] == 1) {
                    // PubTime
                    Calendar calendar = mBook.getPubTime();
                    int year = calendar.get(Calendar.YEAR);
                    if (year == 9999) {
                        entry.add("");
                    } else {
                        int month = calendar.get(Calendar.MONTH) + 1;
                        entry.add(String.valueOf(year) + " - " + month);
                    }
                }
                if (items[5] == 1) {
                    // ISBN
                    entry.add(mBook.getIsbn());
                }
                if (items[6] == 1) {
                    // readingStatus
                    String[] readingStatus = mContext.getResources().getStringArray(R.array.reading_status_array);
                    entry.add(readingStatus[mBook.getReadingStatus()]);
                }
                if (items[7] == 1) {
                    // bookshelf
                    BookShelf bookShelf = BookShelfLab.get(mContext).getBookShelf(mBook.getBookshelfID());
                    entry.add(bookShelf == null ? "" : bookShelf.getTitle());
                }
                if (items[8] == 1) {
                    // labels
                    List<UUID> labelID = mBook.getLabelID();
                    if (labelID.size() != 0) {
                        StringBuilder labelsTitle = new StringBuilder();
                        for (UUID id : labelID) {
                            Label label = LabelLab.get(mContext).getLabel(mBook.getBookshelfID());
                            labelsTitle.append(label == null ? "" : label.getTitle());
                            labelsTitle.append(",");
                        }
                        labelsTitle.deleteCharAt(labelsTitle.length() - 1);
                        entry.add(labelsTitle.toString());
                    } else {
                        entry.add("");
                    }
                }
                if (items[9] == 1) {
                    // notes
                    entry.add(mBook.getNotes());
                }
                if (items[10] == 1) {
                    // website
                    entry.add(mBook.getWebsite());
                }
                csvWriter.writeNext(entry.toArray(new String[0]));
            }
            csvWriter.writeNext(new String[]{""});
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss EEE z");
            String exportTime = format.format(Calendar.getInstance().getTime());
            String exportTimeString = String.format(mContext.getString(R.string.export_csv_file_time), exportTime);
            csvWriter.writeNext(new String[]{exportTimeString});
            csvWriter.writeNext(new String[]{mContext.getString(R.string.export_csv_file_end)});
            csvWriter.writeNext(new String[]{mContext.getString(R.string.export_csv_file_copyright)});
            csvWriter.close();

        } catch (IOException ioe) {
            Log.e(TAG, "csvName = " + mCsvName + ", ioe = " + ioe);
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean isSucceed) {
        AnswersUtil.logContentView(TAG, "Export CSV", "2031", "Backup Result =", isSucceed.toString());
        mDialog.dismiss();
        if (isSucceed) {
            String toastText = String.format(mContext.getString(R.string.export_csv_export_succeed_toast), mCsvName);
            Toast.makeText(mContext, toastText, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.export_csv_export_fail_toast), Toast.LENGTH_LONG).show();
        }
    }
}