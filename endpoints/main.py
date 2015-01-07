#!/usr/bin/env python
#
# Copyright 2007 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
import endpoints
import lib.cloudstorage as gcs
import logging
from datetime import datetime


from google.appengine.api import images
from google.appengine.ext import blobstore
from google.appengine.api.app_identity import get_default_gcs_bucket_name
from protorpc import remote
from protorpc import message_types
from protorpc import messages
from google.appengine.ext import ndb

BUCKET_NAME = get_default_gcs_bucket_name()

WEB_CLIENT_ID = 'replace this with your web client application ID'
ANDROID_CLIENT_ID = 'replace this with your Android client ID'
ANDROID_AUDIENCE = WEB_CLIENT_ID

class IndexNumber:
    def __init__(self):
        self.ind = IndexNumber.boot_index()

    def get_index(self):
        return self.ind

    def inc_index(self):
        self.ind += 1

    @classmethod
    def boot_index(cls):
        stats = gcs.listbucket(path_prefix="/" + BUCKET_NAME)
        vmax = 0

        for obj in stats:
            ind = len('/' + BUCKET_NAME + '/')
            val = int(obj.filename[ind:])
            if val > vmax:
                vmax = val

        return vmax + 1


class UploadRequestMessage(messages.Message):
    filename = messages.StringField(1, required=True)
    photo = messages.BytesField(2, required=True)

class DownloadRequestMessage(messages.Message):
    fetch = messages.IntegerField(1, required=True)
    date_time = messages.StringField(2)

class DownloadResponseMessage(messages.Message):
    filename = messages.StringField(1, required=True)
    date_time = messages.StringField(2, required=True)
    photo = messages.StringField(3, required=True)

class DownloadResponseCollection(messages.Message):
    photos = messages.MessageField(DownloadResponseMessage, 1, repeated=True)

class DeleteRequestMessage(messages.Message):
    filename = messages.StringField(1, required=True)
    is_ndb_entry = messages.BooleanField(2, required=True)

class DeleteResponseMessage(messages.Message):
    message = messages.StringField(1)

class PictureEntry(ndb.Model):
    blob_key = ndb.StringProperty()
    date_time = ndb.DateTimeProperty(auto_now_add=True)
    filename = ndb.StringProperty()
    gcs_filename = ndb.StringProperty()

@endpoints.api(name="testGCS", version="v1",
               allowed_client_ids=[WEB_CLIENT_ID, ANDROID_CLIENT_ID,
                                   endpoints.API_EXPLORER_CLIENT_ID],
               audiences=[ANDROID_AUDIENCE],
               scopes=[endpoints.EMAIL_SCOPE])
class TestGCS(remote.Service):

    num_index = IndexNumber()

    @endpoints.method(UploadRequestMessage, message_types.VoidMessage,
                      path='upload', http_method="POST", name="upload.putphoto")
    def upload_putphoto(self, request):
        filename = request.filename
        bimg = request.photo
        num_index = self.num_index

        gcs_filename = '/' + BUCKET_NAME + '/' + str(num_index.get_index())

        '''
        ok = False
        num = 2

        while ok is False:
            try:
                logging.info("NEL TRY!!!!!!")
                gcs.stat(gcs_filename)
                if num <= 2:
                    logging.info("NELL' IF <= 2!!!!!!")
                    gcs_filename.append(num)
                else:
                    gcs_filename[len(gcs_filename) - 1] = num
                num += 1
            except gcs.NotFoundError:
                logging.info("NELL'EXCEPT!!!!!!")
                ok = True
                pass
        '''
        '''
        stats = gcs.listbucket(path_prefix="/" + bucket_name)
        filenames = []
        num = 2

        for obj in stats:
            filenames.append(obj.filename)

        while gcs_filename in filenames:
            if num <= 2:
                gcs_filename += str(num)
            else:
                gcs_filename[len(gcs_filename) - 1] = str(num)
            num += 1
        '''

        logging.info("gcs_filename = %s" % gcs_filename)

        with gcs.open(gcs_filename, "w", content_type="image/jpeg") as f:
            f.write(bimg)
            f.close()

        bs_filename = "/gs" + gcs_filename
        blob_key = blobstore.create_gs_key(bs_filename)
        pic = PictureEntry(blob_key=blob_key, filename=filename, gcs_filename=gcs_filename)
        pic.put()

        num_index.inc_index()

        return message_types.VoidMessage()

    @endpoints.method(DownloadRequestMessage, DownloadResponseCollection,
                      path='display', http_method="GET", name="display.getphotos")
    def display_getphotos(self, request):
        fetch = request.fetch

        logging.info("DATE_TIME = %s" % request.date_time)

        if request.date_time is None:
            logging.info("NELL'IF!!!!!!!")
            pictures = PictureEntry.query().order(-PictureEntry.date_time).fetch(fetch)
        else:
            logging.info("NELL'ELSE!!!!!!")
            date_time = datetime.strptime(request.date_time, "%Y-%m-%d %H:%M:%S")
            pictures = PictureEntry.query(PictureEntry.date_time < date_time).order(-PictureEntry.date_time).fetch(fetch)

        messlist = []

        for pic in pictures:
            imgurl = images.get_serving_url(blob_key=pic.blob_key)
            date_timeobj = pic.date_time
            date_timestr = date_timeobj.strftime("%Y-%m-%d %H:%M:%S")
            messlist.append(DownloadResponseMessage(filename=pic.filename, date_time=date_timestr, photo=imgurl))

        return DownloadResponseCollection(photos=messlist)

    @endpoints.method(DeleteRequestMessage, DeleteResponseMessage,
                      path='delete', http_method='GET', name='delete.deletephoto')
    def delete_deletephoto(self, request):
        filename = request.filename
        is_ndb_entry = request.is_ndb_entry

        gcs_filename = '/' + BUCKET_NAME + '/' + filename

        if is_ndb_entry:
            qry = PictureEntry.query(PictureEntry.gcs_filename == gcs_filename)
            for pic in qry:
                pic.key.delete()

        try:
            gcs.delete(filename=gcs_filename)

        except gcs.NotFoundError:
            return DownloadResponseMessage(message="File Not Found!!!")

        return DeleteResponseMessage(message="File removed.")


APPLICATION = endpoints.api_server([TestGCS])