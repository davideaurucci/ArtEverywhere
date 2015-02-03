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


# Messages Classes
class UploadRequestMessage(messages.Message):
    filename = messages.StringField(1, required=True)
    photo = messages.BytesField(2, required=True)
    artist = messages.StringField(3, required=True)
    descr = messages.StringField(4)
    luogo = messages.StringField(5)
    dim = messages.StringField(6)

class DownloadRequestMessage(messages.Message):
    fetch = messages.IntegerField(1, required=True)
    date_time = messages.StringField(2)

class DownloadResponseMessage(messages.Message):
    filename = messages.StringField(1, required=True)
    date_time = messages.StringField(2, required=True)
    photo = messages.StringField(3, required=True)
    artist = messages.StringField(4, required=True)
    descr = messages.StringField(5)
    luogo = messages.StringField(6)
    dim = messages.StringField(7)
    likes = messages.IntegerField(8)

class DownloadResponseCollection(messages.Message):
    photos = messages.MessageField(DownloadResponseMessage, 1, repeated=True)

class DeleteRequestMessage(messages.Message):
    filename = messages.StringField(1, required=True)
    is_ndb_entry = messages.BooleanField(2, required=True)

class PictureDetailsMessage(messages.Message):
    gcs_filename = messages.StringField(1, required=True)
    filename = messages.StringField(2, required=True)
    date_time = messages.StringField(3, required=True)
    photo = messages.StringField(4, required=True)
    artist = messages.StringField(5, required=True)
    descr = messages.StringField(6)
    luogo = messages.StringField(7)
    dim = messages.StringField(8)
    likes = messages.IntegerField(9)

class PictureUpdateMessage(messages.Message):
    gcs_filename = messages.StringField(1, required=True)
    filename = messages.StringField(2)
    descr = messages.StringField(3)
    luogo = messages.StringField(4)
    dim = messages.StringField(5)

class PictureDetailsCollection(messages.Message):
    photos = messages.MessageField(PictureDetailsMessage, 1, repeated=True)

class ArtistInfoMessage(messages.Message):
    email = messages.StringField(1, required=True)
    nome = messages.StringField(2, required=True)
    cognome = messages.StringField(3, required=True)
    nickname = messages.StringField(4)
    sito = messages.StringField(5)
    bio = messages.StringField(6)
    pic = messages.StringField(7)

class ArtistUpdateMessage(messages.Message):
    email = messages.StringField(1, required=True)
    new_email = messages.StringField(2)
    new_nome = messages.StringField(3)
    new_cognome = messages.StringField(4)
    new_nickname = messages.StringField(5)
    new_sito = messages.StringField(6)
    new_bio = messages.StringField(7)
    new_pic = messages.StringField(8)

class ArtistDetailsMessage(messages.Message):
    message = messages.StringField(1)
    email = messages.StringField(2)
    nome = messages.StringField(3)
    cognome = messages.StringField(4)
    nickname = messages.StringField(5)
    sito = messages.StringField(6)
    bio = messages.StringField(7)
    pic = messages.StringField(8)

class ArtistRequestMessage(messages.Message):
    email = messages.StringField(1, required=True)

class DefaultResponseMessage(messages.Message):
    message = messages.StringField(1)

# Datastore Tables
class PictureEntry(ndb.Model):
    blob_key = ndb.StringProperty(required=True)
    date_time = ndb.DateTimeProperty(auto_now_add=True)
    filename = ndb.StringProperty(required=True)
    artist = ndb.StringProperty(required=True)
    gcs_filename = ndb.StringProperty(required=True)
    descr = ndb.StringProperty()
    luogo = ndb.StringProperty()
    dim = ndb.StringProperty()
    likes = ndb.IntegerProperty()

class ArtistEntry(ndb.Model):
    email = ndb.StringProperty(required=True)
    nome = ndb.StringProperty(required=True)
    cognome = ndb.StringProperty(required=True)
    nickname = ndb.StringProperty()
    sito = ndb.StringProperty()
    bio = ndb.StringProperty()
    pic = ndb.StringProperty()

class TechniqueEntry(ndb.Model):
    nome = ndb.StringProperty(required=True)

@endpoints.api(name="testGCS", version="v1",
               allowed_client_ids=[WEB_CLIENT_ID, ANDROID_CLIENT_ID,
                                   endpoints.API_EXPLORER_CLIENT_ID],
               audiences=[ANDROID_AUDIENCE],
               scopes=[endpoints.EMAIL_SCOPE])
class TestGCS(remote.Service):
    def __init__(self):
        self.num_index = IndexNumber()

    @endpoints.method(UploadRequestMessage, DefaultResponseMessage,
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

        artist = ArtistEntry.query(ArtistEntry.email == request.artist).get()

        if artist is None:
            return DefaultResponseMessage(message="Artist not found!")

        pic = PictureEntry(parent=artist.key, blob_key=blob_key, filename=filename, gcs_filename=gcs_filename,
                           artist=request.artist, descr=request.descr, luogo=request.luogo, dim=request.dim, likes=0)
        pic.put()

        num_index.inc_index()

        return DefaultResponseMessage(message="Artwork added!")

    @endpoints.method(DownloadRequestMessage, DownloadResponseCollection,
                      path='display', http_method="GET", name="display.getphotos")
    def display_getphotos(self, request):
        fetch = request.fetch

        logging.info("DATE_TIME = %s" % request.date_time)

        if request.date_time is None:
            pictures = PictureEntry.query().order(-PictureEntry.date_time).fetch(fetch)
        else:
            date_time = datetime.strptime(request.date_time, "%Y-%m-%d %H:%M:%S")
            pictures = PictureEntry.query(PictureEntry.date_time < date_time).order(-PictureEntry.date_time).fetch(fetch)

        messlist = []

        for pic in pictures:
            imgurl = images.get_serving_url(blob_key=pic.blob_key)
            date_timeobj = pic.date_time
            date_timestr = date_timeobj.strftime("%Y-%m-%d %H:%M:%S")
            messlist.append(DownloadResponseMessage(artist=pic.artist, filename=pic.filename, date_time=date_timestr,
                                                    photo=imgurl, descr=pic.descr, luogo=pic.luogo, dim=pic.dim,
                                                    likes=pic.likes))

        return DownloadResponseCollection(photos=messlist)

    @endpoints.method(DeleteRequestMessage, DefaultResponseMessage,
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

        return DefaultResponseMessage(message="File removed.")

    @endpoints.method(ArtistInfoMessage, DefaultResponseMessage,
                      path='registration', http_method='GET', name='registration.registerartist')
    def register_artist(self, request):
        artist = ArtistEntry.query(ArtistEntry.email == request.email)

        if artist.get() is not None:
            return DefaultResponseMessage(message="Artist already exists!")


        email = request.email
        nome = request.nome
        cognome = request.cognome
        nickname = request.nickname
        sito = request.sito
        bio = request.bio
        pic = request.pic

        artist = ArtistEntry(email=email, nome=nome, cognome=cognome,
                             nickname=nickname, sito=sito, bio=bio, pic=pic)

        artist.put()

        return DefaultResponseMessage(message="Artist registered")

    @endpoints.method(ArtistRequestMessage, ArtistDetailsMessage,
                      path='getinfo', http_method='GET', name='getinfo.getartist')
    def get_artist(self, request):
        artist = ArtistEntry.query(ArtistEntry.email == request.email).get()

        if artist is None:
            return ArtistDetailsMessage("Artist Not Found!")

        return ArtistDetailsMessage(email=artist.email, nome=artist.nome,
                                    cognome=artist.cognome, nickname=artist.nickname,
                                    sito=artist.sito, bio=artist.bio, pic=artist.pic)

    @endpoints.method(ArtistUpdateMessage, DefaultResponseMessage,
                      path='artistinfo', http_method='GET', name='artistinfo.updateartist')
    def update_artist(self, request):
        artist = ArtistEntry.query(ArtistEntry.email == request.email).get()

        if artist is None:
            return DefaultResponseMessage(message="Artist not found!")

        if request.new_email is not None:
            artist.email = request.new_email
        if request.new_nome is not None:
            artist.nome = request.new_nome
        if request.new_cognome is not None:
            artist.cognome = request.new_cognome
        if request.new_nickname is not None:
            artist.nickname = request.new_nickname
        if request.new_sito is not None:
            artist.sito = request.new_sito
        if request.new_bio is not None:
            artist.bio = request.new_bio
        if request.new_pic is not None:
            artist.pic = request.new_pic

        artist.put()

        return DefaultResponseMessage(message="Artist updated!")

    @endpoints.method(PictureUpdateMessage, DefaultResponseMessage,
                      path='picinfo', http_method='GET', name='picinfo.updatepicture')
    def update_picture(self, request):
        picture = PictureEntry.query(PictureEntry.gcs_filename == request.gcs_filename).get()

        if picture is None:
            return DefaultResponseMessage(message="Picture not found!")

        if request.filename is not None:
            picture.filename = request.filename
        if request.descr is not None:
            picture.descr = request.descr
        if request.luogo is not None:
            picture.luogo = request.luogo
        if request.dim is not None:
            picture.dim = request.dim

        picture.put()

        return DefaultResponseMessage(message="Picture Updated!")


    @endpoints.method(ArtistRequestMessage, PictureDetailsCollection,
                      path='artworks', http_method='GET', name='artworks.getartworks')
    def get_artworks(self, request):
        artist = ArtistEntry.query(ArtistEntry.email == request.email).get()

        artworks = PictureEntry.query(ancestor=artist.key).order(-PictureEntry.date_time)

        messlist = []

        for pic in artworks:
            imgurl = images.get_serving_url(blob_key=pic.blob_key)
            date_timeobj = pic.date_time
            date_timestr = date_timeobj.strftime("%Y-%m-%d %H:%M:%S")
            messlist.append(PictureDetailsMessage(artist=pic.artist, gcs_filename=pic.gcs_filename,
                                                  filename=pic.filename, date_time=date_timestr,photo=imgurl,
                                                  descr=pic.descr, luogo=pic.luogo, dim=pic.dim, likes=pic.likes))

        return PictureDetailsCollection(photos=messlist)


APPLICATION = endpoints.api_server([TestGCS])