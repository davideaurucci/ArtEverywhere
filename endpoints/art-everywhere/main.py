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
import gdata.photos.service
import gdata.media
import gdata.geo
import atom
import logging
import base64
from cStringIO import StringIO

from protorpc import remote
from protorpc import message_types
from protorpc import messages
from google.appengine.api import mail
from google.appengine.ext import ndb
from datetime import datetime
from datetime import timedelta

WEB_CLIENT_ID = '803782534144-6n7lv3ii2a49le1q8arifv5cltglj4em.apps.googleusercontent.com'
ANDROID_CLIENT_ID = '803782534144-ldi6pbntu4b298061c4a4anrldlj34ai.apps.googleusercontent.com'
ANDROID_AUDIENCE = WEB_CLIENT_ID
USERNAME = 'arteverywhere00@gmail.com'


# Datastore Tables
class TechniqueEntry(ndb.Model):
    nome = ndb.StringProperty(required=True)

class PictureEntry(ndb.Model):
    url = ndb.StringProperty(required=True)
    photo_id = ndb.StringProperty(required=True)
    date_time = ndb.DateTimeProperty(auto_now_add=True)
    title = ndb.StringProperty(required=True)
    artist = ndb.StringProperty(required=True)
    descr = ndb.StringProperty()
    luogo = ndb.StringProperty()
    dim = ndb.StringProperty()
    likes = ndb.IntegerProperty()
    technique = ndb.KeyProperty(kind=TechniqueEntry)

class ArtistEntry(ndb.Model):
    email = ndb.StringProperty(required=True)
    nome = ndb.StringProperty(required=True)
    cognome = ndb.StringProperty(required=True)
    nickname = ndb.StringProperty()
    sito = ndb.StringProperty()
    bio = ndb.StringProperty()
    pic = ndb.StringProperty(required=True)

class CommentEntry(ndb.Model):
    comment = ndb.StringProperty(required=True)
    email = ndb.StringProperty(required=True)
    id = ndb.StringProperty()
    date_time = ndb.DateTimeProperty(auto_now_add=True)

class LikeEntry(ndb.Model):
    url = ndb.StringProperty(required=True)
    email = ndb.StringProperty(required=True)

# Message Classes
class DefaultResponseMessage(messages.Message):
    message = messages.StringField(1)

class UploadRequestMessage(messages.Message):
    title = messages.StringField(1, required=True)
    image = messages.StringField(2, required=True)
    artist = messages.StringField(3, required=True)
    technique = messages.StringField(4, required=True)
    descr = messages.StringField(5)
    luogo = messages.StringField(6)
    dim = messages.StringField(7)

class DeleteRequestMessage(messages.Message):
    url = messages.StringField(1, required=True)

class PictureUpdateMessage(messages.Message):
    url = messages.StringField(1, required=True)
    new_title = messages.StringField(2)
    new_technique = messages.StringField(3)
    new_descr = messages.StringField(4)
    new_luogo = messages.StringField(5)
    new_dim = messages.StringField(6)

class ArtistInfoMessage(messages.Message):
    email = messages.StringField(1, required=True)
    nome = messages.StringField(2, required=True)
    cognome = messages.StringField(3, required=True)
    nickname = messages.StringField(4)
    sito = messages.StringField(5)
    bio = messages.StringField(6)
    pic = messages.StringField(7, required=True)

class ArtistUpdateMessage(messages.Message):
    email = messages.StringField(1, required=True)
    new_email = messages.StringField(2)
    new_nome = messages.StringField(3)
    new_cognome = messages.StringField(4)
    new_nickname = messages.StringField(5)
    new_sito = messages.StringField(6)
    new_bio = messages.StringField(7)
    new_pic = messages.StringField(8)

class ArtistRequestMessage(messages.Message):
    email = messages.StringField(1, required=True)

class ArtistDetailsMessage(messages.Message):
    message = messages.StringField(1)
    email = messages.StringField(2)
    nome = messages.StringField(3)
    cognome = messages.StringField(4)
    nickname = messages.StringField(5)
    sito = messages.StringField(6)
    bio = messages.StringField(7)
    pic = messages.StringField(8)

class PictureDetailsMessage(messages.Message):
    title = messages.StringField(1, required=True)
    date_time = messages.StringField(2, required=True)
    url = messages.StringField(3, required=True)
    artist = messages.StringField(4, required=True)
    technique = messages.StringField(5)
    descr = messages.StringField(6)
    luogo = messages.StringField(7)
    dim = messages.StringField(8)
    likes = messages.IntegerField(9)

class DownloadRequestMessage(messages.Message):
    fetch = messages.IntegerField(1, required=True)
    date_time = messages.StringField(2)

class DownloadResponseMessage(messages.Message):
    title = messages.StringField(1, required=True)
    date_time = messages.StringField(2, required=True)
    url = messages.StringField(3, required=True)
    artist = messages.StringField(4, required=True)
    technique = messages.StringField(5)
    descr = messages.StringField(6)
    luogo = messages.StringField(7)
    dim = messages.StringField(8)
    likes = messages.IntegerField(9)

class DownloadResponseCollection(messages.Message):
    photos = messages.MessageField(DownloadResponseMessage, 1, repeated=True)
    message = messages.StringField(2)

class PictureDetailsCollection(messages.Message):
    photos = messages.MessageField(PictureDetailsMessage, 1, repeated=True)
    message = messages.StringField(2)

class RefreshRequestMessage(messages.Message):
    fetch = messages.IntegerField(1, required=True)
    date_time = messages.StringField(2, required=True)
    technique = messages.StringField(3)
    luogo = messages.StringField(4)

class TechniqueResponseMessage(messages.Message):
    technique = messages.StringField(1, required=True)

class TechniqueResponseCollection(messages.Message):
    techniques = messages.MessageField(TechniqueResponseMessage, 1, repeated=True)

class InsertCommentMessage(messages.Message):
    comment = messages.StringField(1, required=True)
    url = messages.StringField(2, required=True)
    email = messages.StringField(3)

class DeleteCommentMessage(messages.Message):
    id = messages.StringField(1, required=True)

class CommentListMessage(messages.Message):
    url = messages.StringField(1, required=True)
    fetch = messages.IntegerField(2)

class CommentDetails(messages.Message):
    comment = messages.StringField(1, required=True)
    email = messages.StringField(2, required=True)
    id = messages.StringField(3, required=True)
    pic = messages.StringField(4)
    name = messages.StringField(5)
    date = messages.StringField(6, required=True)

class CommentDetailsCollection(messages.Message):
    comments = messages.MessageField(CommentDetails, 1, repeated=True)
    message = messages.StringField(2)

class GeneralLikeRequest(messages.Message):
    url = messages.StringField(1, required=True)
    email = messages.StringField(2, required=True)

class GeneralLikeResponse(messages.Message):
    hasLiked = messages.BooleanField(1, required=True)
    likes = messages.IntegerField(2, required=True)
    message = messages.StringField(3)

class TechniqueSearchRequest(messages.Message):
    fetch = messages.IntegerField(1, required=True)
    date_time = messages.StringField(2)
    technique = messages.StringField(3, required=True)

class PlaceSearchRequest(messages.Message):
    fetch = messages.IntegerField(1, required=True)
    date_time = messages.StringField(2)
    luogo = messages.StringField(3, required=True)

class ArtistBriefMessage(messages.Message):
    email = messages.StringField(1, required=True)
    nome = messages.StringField(2, required=True)
    cognome = messages.StringField(3, required=True)
    nickname = messages.StringField(4)
    pic = messages.StringField(5)

class ArtistBriefCollection(messages.Message):
    artists = messages.MessageField(ArtistBriefMessage, 1, repeated=True)
    message = messages.StringField(2)

@endpoints.api(name="artEverywhere", version="v1",
               allowed_client_ids=[WEB_CLIENT_ID, ANDROID_CLIENT_ID,
                                   endpoints.API_EXPLORER_CLIENT_ID],
               audiences=[ANDROID_AUDIENCE],
               scopes=[endpoints.EMAIL_SCOPE])
class PicasaWA(remote.Service):
    album_id = None

    def __init__(self):
        gd_client = PicasaWA.login()

        albums = gd_client.GetUserFeed(user=USERNAME)
        for album in albums.entry:
            if album.title.text == 'ArtEverywhere':
                PicasaWA.album_id = album.gphoto_id.text

        if PicasaWA.album_id is None:
            PicasaWA.album_id = gd_client.InsertAlbum(title='ArtEverywhere', summary='ArtEverywhere').gphoto_id.text

    @endpoints.method(UploadRequestMessage, DefaultResponseMessage,
                      path='upload', http_method="POST", name="upload.putphoto")
    def upload_putphoto(self, request):
        artist = ArtistEntry.query(ArtistEntry.email == request.artist.lower()).get()
        if artist is None:
            return DefaultResponseMessage(message="Artist not found!")

        technique = TechniqueEntry.query(TechniqueEntry.nome == request.technique).get()
        if technique is None:
            return DefaultResponseMessage(message="Technique not found!")

        decoded_img_str = base64.decodestring(request.image)
        imgfile = StringIO(decoded_img_str)

        gd_client = PicasaWA.login()
        album_url = '/data/feed/api/user/%s/albumid/%s' % (USERNAME, PicasaWA.album_id)

        metadata = gdata.photos.PhotoEntry()
        metadata.title = atom.Title(text=request.title)
        tags = '%s, %s, %s' % (request.artist.lower(), request.technique, request.luogo)
        keywords = gdata.media.Keywords()
        keywords.text = tags
        metadata.media.keywords = keywords

        photo = gd_client.InsertPhoto(album_url, metadata, imgfile, content_type='image/jpeg')

        pic = PictureEntry(parent=artist.key, title=request.title, url=photo.content.src,
                           descr=request.descr, luogo=request.luogo, artist=artist.email,
                           dim=request.dim, likes=0, technique=technique.key, photo_id=photo.gphoto_id.text)
        pic.put()
        return DefaultResponseMessage(message="Artwork added!")

    @endpoints.method(DeleteRequestMessage, DefaultResponseMessage,
                      path='delete', http_method='GET', name='delete.deletephoto')
    def delete_deletephoto(self, request):
        pic = PictureEntry.query(PictureEntry.url == request.url).get()

        if pic is None:
            return DefaultResponseMessage(message="Artwork not found!")

        comments = CommentEntry.query(ancestor=pic.key)

        for comment in comments:
            comment.key.delete()

        likes = LikeEntry.query(ancestor=pic.key)

        for like in likes:
            like.key.delete()

        gd_client = PicasaWA.login()

        photos = gd_client.GetFeed('/data/feed/api/user/%s/albumid/%s?kind=photo' % (USERNAME, PicasaWA.album_id))
        for photo in photos.entry:
            if photo.gphoto_id.text == pic.photo_id:
                gd_client.Delete(photo)

        pic.key.delete()

        return DefaultResponseMessage(message="Artwork removed!")

    @endpoints.method(PictureUpdateMessage, DefaultResponseMessage,
                      path='picinfo', http_method='GET', name='picinfo.updatepicture')
    def update_picture(self, request):
        photo = PictureEntry.query(PictureEntry.url == request.url).get()

        if photo is None:
            return DefaultResponseMessage(message="Artwork not found!")

        gd_client = PicasaWA.login()
        photo_entry = None

        photos = gd_client.GetFeed('/data/feed/api/user/%s/albumid/%s?kind=photo' % (USERNAME, PicasaWA.album_id))
        for pic in photos.entry:
            if photo.photo_id == pic.gphoto_id.text:
                photo_entry = pic
                break

        if request.new_title is not None:
            photo.title = request.new_title
            if photo_entry.title.text is not None:
                photo_entry.title.text = request.new_title
            else:
                photo_entry.title = atom.Title(text=request.new_title)
        if request.new_descr is not None:
            photo.descr = request.new_descr
        if request.new_luogo is not None:
            photo.luogo = request.new_luogo
            technique = TechniqueEntry.query(TechniqueEntry.key == photo.technique).get()
            tags = '%s, %s, %s' % (photo.artist, technique.nome, request.new_luogo)
            if photo_entry.media.keywords.text is not None:
                photo_entry.media.keywords.text = tags
            else:
                keywords = gdata.media.Keywords()
                keywords.text = tags
                photo_entry.media.keywords = keywords
        if request.new_dim is not None:
            photo.dim = request.new_dim
        if request.new_technique is not None:
            technique = TechniqueEntry.query(TechniqueEntry.nome == request.new_technique).get()
            if technique is not None:
                photo.technique = technique.key
                tags = '%s, %s, %s' % (photo.artist, technique.nome, photo.luogo)
                if photo_entry.media.keywords.text is not None:
                    photo_entry.media.keywords.text = tags
                else:
                    keywords = gdata.media.Keywords()
                    keywords.text = tags
                    photo_entry.media.keywords = keywords
            else:
                return DefaultResponseMessage(message="Technique not found!")

        gd_client.UpdatePhotoMetadata(photo_entry)
        photo.put()

        return DefaultResponseMessage(message="Artwork Updated!")

    @endpoints.method(ArtistInfoMessage, DefaultResponseMessage,
                      path='registration', http_method='POST', name='registration.registerartist')
    def register_artist(self, request):
        artist = ArtistEntry.query(ArtistEntry.email == request.email.lower()).get()

        if artist is not None:
            return DefaultResponseMessage(message="Artist already exists!")

        email = request.email
        nome = request.nome
        cognome = request.cognome
        nickname = request.nickname
        sito = request.sito
        bio = request.bio
        pic = request.pic

        artist = ArtistEntry(email=email.lower(), nome=nome, cognome=cognome,
                             nickname=nickname, sito=sito, bio=bio, pic=pic)

        artist.put()

        message = mail.EmailMessage(sender="arteverywhere00@gmail.com",
                                    subject="Grazie per esserti registrato ad ArtEverywhere!!")

        message.to = '%s %s <%s>' % (nome, cognome, email)
        message.body = """
        Caro %s:

        Il tuo account di ArtEverywhere e' stato attivato. Ora puoi usare
        l'app per mobile e accedere usando il tuo account Google
        per utilizzare le funzionalita' dell'app.

        Per ogni domanda o suggerimento, sentiti libero di contattarci.

        Il team di ArtEverywhere.
        """ % nome

        message.html = """
        <html>
            <head></head>
            <body>
                <p align="center"><img alt="" height="100" src="https://lh4.googleusercontent.com/-4EUhV6V6U9Y/VN24VjX-oDI/AAAAAAAAABw/I9ZVh-fYSXs/s798-no/ae.png" width="100"></p>
                <hr>

                <p>Caro %s:</p>
                <div>Il tuo account di ArtEverywhere e' stato attivato.</div>
                <div>Ora puoi usare l'app per mobile e accedere usando il tuo account Google
                per utilizzare le funzionalita' dell'app.</div>
                <div>Per ogni domanda o suggerimento, sentiti libero di contattarci.</div>

                <p>Il team di ArtEverywhere.</p>
            </body>
        </html>
        """ % nome

        message.send()

        return DefaultResponseMessage(message="Artist registered!")

    @endpoints.method(ArtistUpdateMessage, DefaultResponseMessage,
                      path='artistinfo', http_method='GET', name='artistinfo.updateartist')
    def update_artist(self, request):
        artist = ArtistEntry.query(ArtistEntry.email == request.email.lower()).get()

        if artist is None:
            return DefaultResponseMessage(message="Artist not found!")

        if request.new_email is not None:
            artist.email = request.new_email.lower()
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

    @endpoints.method(ArtistRequestMessage, ArtistDetailsMessage,
                      path='getinfo', http_method='GET', name='getinfo.getartist')
    def get_artist(self, request):
        artist = ArtistEntry.query(ArtistEntry.email == request.email.lower()).get()

        if artist is None:
            return ArtistDetailsMessage(message="Artist Not Found!")

        return ArtistDetailsMessage(email=artist.email, nome=artist.nome,
                                    cognome=artist.cognome, nickname=artist.nickname,
                                    sito=artist.sito, bio=artist.bio, pic=artist.pic)

    @endpoints.method(message_types.VoidMessage, TechniqueResponseCollection,
                      path='techniques', http_method='GET', name='techniques.gettechniques')
    def get_techniques(self, request):
        techniques = TechniqueEntry.query().order(TechniqueEntry.nome)

        messlist = []

        for technique in techniques:
            messlist.append(TechniqueResponseMessage(technique=technique.nome))

        return TechniqueResponseCollection(techniques=messlist)

    @endpoints.method(TechniqueResponseMessage, DefaultResponseMessage,
                      path='technique', http_method='GET', name='technique.puttechnique')
    def put_technique(self, request):
        entry = TechniqueEntry.query(TechniqueEntry.nome == request.technique).get()
        if entry is not None:
            return DefaultResponseMessage(message="Technique already existent!")

        technique = TechniqueEntry(nome=request.technique)
        technique.put()

        return DefaultResponseMessage(message="Technique Added")

    @endpoints.method(ArtistRequestMessage, DefaultResponseMessage,
                      path='check', http_method='POST', name='check.checklogin')
    def check_login(self, request):
        artist = ArtistEntry.query(ArtistEntry.email == request.email.lower()).get()

        if artist is not None:
            return DefaultResponseMessage(message="Artist Registered!")
        else:
            return DefaultResponseMessage(message="Artist Not Found!")

    @endpoints.method(ArtistRequestMessage, PictureDetailsCollection,
                      path='artworks', http_method='GET', name='artworks.getartworks')
    def get_artworks(self, request):
        artist = ArtistEntry.query(ArtistEntry.email == request.email.lower()).get()

        if artist is None:
            return PictureDetailsCollection(message="Artist Not Found!")

        artworks = PictureEntry.query(ancestor=artist.key).order(-PictureEntry.date_time)

        messlist = []

        for pic in artworks:
            date_timeobj = pic.date_time
            date_timestr = date_timeobj.strftime("%Y-%m-%d %H:%M:%S")
            technique = TechniqueEntry.query(TechniqueEntry.key == pic.technique).get()
            messlist.append(PictureDetailsMessage(artist=pic.artist, title=pic.title, date_time=date_timestr,
                                                  url=pic.url, descr=pic.descr, luogo=pic.luogo, dim=pic.dim,
                                                  likes=pic.likes, technique=technique.nome))

        return PictureDetailsCollection(photos=messlist)
    '''
    @endpoints.method(DownloadRequestMessage, DownloadResponseCollection,
                      path='display', http_method="GET", name="display.getphotos")
    def display_getphotos(self, request):
        fetch = request.fetch
        pictures = None

        if request.date_time is None and request.technique is None:
            pictures = PictureEntry.query().order(-PictureEntry.date_time).fetch(fetch)
        elif request.technique is None:
            date_time = datetime.strptime(request.date_time, "%Y-%m-%d %H:%M:%S")
            pictures = PictureEntry.query(PictureEntry.date_time < date_time).order(-PictureEntry.date_time).fetch(fetch)
        elif request.date_time is None:
            technique = TechniqueEntry.query(TechniqueEntry.nome == request.technique).get()
            if technique is not None:
                pictures = PictureEntry.query(PictureEntry.technique == technique.key).order(-PictureEntry.date_time).fetch(fetch)
        else:
            date_time = datetime.strptime(request.date_time, "%Y-%m-%d %H:%M:%S")
            technique = TechniqueEntry.query(TechniqueEntry.nome == request.technique).get()
            if technique is not None:
                pictures = PictureEntry.query(ndb.AND(PictureEntry.technique == technique.key,
                                                      PictureEntry.date_time < date_time)).order(-PictureEntry.date_time).fetch(fetch)

        messlist = []
        if pictures is not None:
            for pic in pictures:
                date_timeobj = pic.date_time
                date_timestr = date_timeobj.strftime("%Y-%m-%d %H:%M:%S")
                technique = TechniqueEntry.query(TechniqueEntry.key == pic.technique).get()
                messlist.append(DownloadResponseMessage(artist=pic.artist, title=pic.title,
                                                        date_time=date_timestr, url=pic.url, descr=pic.descr,
                                                        luogo=pic.luogo, dim=pic.dim, likes=pic.likes,
                                                        technique=technique.nome))

        return DownloadResponseCollection(photos=messlist)
    '''

    @endpoints.method(DownloadRequestMessage, DownloadResponseCollection,
                      path='display', http_method="GET", name="display.getphotos")
    def display_getphotos(self, request):
        fetch = request.fetch

        if request.date_time is None:
            pictures = PictureEntry.query().order(-PictureEntry.date_time).fetch(fetch)
        else:
            date_time = datetime.strptime(request.date_time, "%Y-%m-%d %H:%M:%S")
            pictures = PictureEntry.query(PictureEntry.date_time < date_time).order(-PictureEntry.date_time).fetch(fetch)

        messlist = []
        if pictures is not None:
            for pic in pictures:
                date_timeobj = pic.date_time
                date_timestr = date_timeobj.strftime("%Y-%m-%d %H:%M:%S")
                technique = TechniqueEntry.query(TechniqueEntry.key == pic.technique).get()
                messlist.append(DownloadResponseMessage(artist=pic.artist, title=pic.title,
                                                        date_time=date_timestr, url=pic.url, descr=pic.descr,
                                                        luogo=pic.luogo, dim=pic.dim, likes=pic.likes,
                                                        technique=technique.nome))

        return DownloadResponseCollection(photos=messlist)

    @endpoints.method(TechniqueSearchRequest, DownloadResponseCollection,
                      path='displaytechnique', http_method="GET", name="displaytechnique.getphotos")
    def displaytechnique_getphotos(self, request):
        fetch = request.fetch
        technique = TechniqueEntry.query(TechniqueEntry.nome == request.technique).get()

        if technique is None:
            return DownloadResponseCollection(message="Technique not found!")

        if request.date_time is None:
            pictures = PictureEntry.query(PictureEntry.technique == technique.key).order(-PictureEntry.date_time).fetch(fetch)
        else:
            date_time = datetime.strptime(request.date_time, "%Y-%m-%d %H:%M:%S")
            pictures = PictureEntry.query(ndb.AND(PictureEntry.technique == technique.key,
                                                  PictureEntry.date_time < date_time)).order(-PictureEntry.date_time).fetch(fetch)

        messlist = []
        if pictures is not None:
            for pic in pictures:
                date_timeobj = pic.date_time
                date_timestr = date_timeobj.strftime("%Y-%m-%d %H:%M:%S")
                technique = TechniqueEntry.query(TechniqueEntry.key == pic.technique).get()
                messlist.append(DownloadResponseMessage(artist=pic.artist, title=pic.title,
                                                        date_time=date_timestr, url=pic.url, descr=pic.descr,
                                                        luogo=pic.luogo, dim=pic.dim, likes=pic.likes,
                                                        technique=technique.nome))

        return DownloadResponseCollection(photos=messlist)

    @endpoints.method(PlaceSearchRequest, DownloadResponseCollection,
                      path='displayplace', http_method="GET", name="displayplace.getphotos")
    def displayplaces_getphotos(self, request):
        fetch = request.fetch

        if request.date_time is None:
            pictures = PictureEntry.query(PictureEntry.luogo == request.luogo).order(-PictureEntry.date_time).fetch(fetch)
        else:
            date_time = datetime.strptime(request.date_time, "%Y-%m-%d %H:%M:%S")
            pictures = PictureEntry.query(ndb.AND(PictureEntry.luogo == request.luogo,
                                                  PictureEntry.date_time < date_time)).order(-PictureEntry.date_time).fetch(fetch)

        messlist = []
        if pictures is not None:
            for pic in pictures:
                date_timeobj = pic.date_time
                date_timestr = date_timeobj.strftime("%Y-%m-%d %H:%M:%S")
                technique = TechniqueEntry.query(TechniqueEntry.key == pic.technique).get()
                messlist.append(DownloadResponseMessage(artist=pic.artist, title=pic.title,
                                                        date_time=date_timestr, url=pic.url, descr=pic.descr,
                                                        luogo=pic.luogo, dim=pic.dim, likes=pic.likes,
                                                        technique=technique.nome))

        return DownloadResponseCollection(photos=messlist)

    @endpoints.method(RefreshRequestMessage, DownloadResponseCollection,
                      path='refresh', http_method="GET", name="refresh.refreshphotos")
    def refresh_photos(self, request):
        fetch = request.fetch
        pictures = None
        extra_second = timedelta(seconds=1)

        if request.technique is not None and request.luogo is not None:
            return DownloadResponseCollection(message="Both technique and luogo are not None!")

        if request.technique is None and request.luogo is None:
            try:
                date_time = datetime.strptime(request.date_time, "%Y-%m-%d %H:%M:%S")
            except ValueError:
                return DownloadResponseCollection(message="Bad date found!!")
            query_date = date_time + extra_second
            pictures = PictureEntry.query(PictureEntry.date_time > query_date).order(-PictureEntry.date_time).fetch(fetch)
        elif request.technique is None:
            try:
                date_time = datetime.strptime(request.date_time, "%Y-%m-%d %H:%M:%S")
            except ValueError:
                return DownloadResponseCollection(message="Bad date found!!")
            query_date = date_time + extra_second
            pictures = PictureEntry.query(ndb.AND(PictureEntry.luogo == request.luogo,
                                                  PictureEntry.date_time > query_date)).order(-PictureEntry.date_time).fetch(fetch)
        else:
            try:
                date_time = datetime.strptime(request.date_time, "%Y-%m-%d %H:%M:%S")
            except ValueError:
                return DownloadResponseCollection(message="Bad date found!!")
            query_date = date_time + extra_second
            technique = TechniqueEntry.query(TechniqueEntry.nome == request.technique).get()
            if technique is not None:
                pictures = PictureEntry.query(ndb.AND(PictureEntry.technique == technique.key,
                                                      PictureEntry.date_time > query_date)).order(-PictureEntry.date_time).fetch(fetch)

        messlist = []

        for pic in pictures:
            date_timeobj = pic.date_time
            date_timestr = date_timeobj.strftime("%Y-%m-%d %H:%M:%S")
            technique = TechniqueEntry.query(TechniqueEntry.key == pic.technique).get()
            messlist.append(DownloadResponseMessage(artist=pic.artist, title=pic.title,
                                                        date_time=date_timestr, url=pic.url, descr=pic.descr,
                                                        luogo=pic.luogo, dim=pic.dim, likes=pic.likes,
                                                        technique=technique.nome))

        if len(messlist) == 0:
            return DownloadResponseCollection(message="No new artworks found!")

        return DownloadResponseCollection(photos=messlist)



    @endpoints.method(message_types.VoidMessage, ArtistBriefCollection,
                      path='artistlist', http_method="GET", name="artistlist.getartists")
    def artistlist_getartist(self, request):
        artists = ArtistEntry.query().order(ArtistEntry.cognome, ArtistEntry.nome)

        if artists is None:
            return ArtistBriefCollection(message="No artist found!")

        messlist = []

        for artist in artists:
            messlist.append(ArtistBriefMessage(email=artist.email, nome=artist.nome, cognome=artist.cognome,
                                               nickname=artist.nickname, pic=artist.pic))

        return ArtistBriefCollection(artists=messlist)

    @endpoints.method(InsertCommentMessage, DefaultResponseMessage,
                      path='comment', http_method='POST', name='comment.insertcomment')
    def insert_comment(self, request):
        photo = PictureEntry.query(PictureEntry.url == request.url).get()

        if photo is None:
            return DefaultResponseMessage(message="Artwork not found!")

        comment = CommentEntry(parent=photo.key, comment=request.comment, email=request.email.lower())
        key = comment.put()
        comment = key.get()
        comment.id = str(key.id())
        comment.put()

        return DefaultResponseMessage(message="Comment Added!!")

    @endpoints.method(DeleteCommentMessage, DefaultResponseMessage,
                      path='remove', http_method='GET', name='remove.removecomment')
    def remove_comment(self, request):
        comment = CommentEntry.query(CommentEntry.id == request.id).get()

        if comment is None:
            return DefaultResponseMessage(message="Comment not found!")

        comment.key.delete()

        return DefaultResponseMessage(message="Comment removed!")

    @endpoints.method(CommentListMessage, CommentDetailsCollection,
                      path='list', http_method='GET', name='list.commentlist')
    def comment_list(self, request):
        photo = PictureEntry.query(PictureEntry.url == request.url).get()

        if photo is None:
            return DefaultResponseMessage(message="Artwork not found!")

        if request.fetch is None:
            comments = CommentEntry.query(ancestor=photo.key).order(CommentEntry.date_time)
        else:
            comments = CommentEntry.query(ancestor=photo.key).order(CommentEntry.date_time).fetch(request.fetch)

        messlist = []

        if comments is None:
            return CommentDetailsCollection(message="No comments found!")

        for comment in comments:
            date_timeobj = comment.date_time
            date_timestr = date_timeobj.strftime("%Y-%m-%d %H:%M:%S")
            artist = ArtistEntry.query(ArtistEntry.email == comment.email).get()
            if artist is not None:
                pic = artist.pic
                messlist.append(CommentDetails(comment=comment.comment, email=comment.email, id=comment.id,
                                               pic=pic, name="%s %s" % (artist.nome, artist.cognome), date=date_timestr))
            else:
                messlist.append(CommentDetails(comment=comment.comment, email=comment.email, id=comment.id, date=date_timestr))

        return CommentDetailsCollection(comments=messlist)

    @endpoints.method(GeneralLikeRequest, GeneralLikeResponse,
                      path='like', http_method='POST', name='like.likestatus')
    def like_status(self, request):
        photo = PictureEntry.query(PictureEntry.url == request.url).get()

        if photo is None:
            return GeneralLikeResponse(message="Artwork not found!")

        like = LikeEntry.query(ndb.AND(LikeEntry.url == request.url,
                                       LikeEntry.email == request.email.lower())).get()

        if like is None:
            return GeneralLikeResponse(hasLiked=False, likes=photo.likes)
        else:
            return GeneralLikeResponse(hasLiked=True, likes=photo.likes)

    @endpoints.method(GeneralLikeRequest, GeneralLikeResponse,
                      path='change', http_method='POST', name='change.changelikestatus')
    def change_like_status(self, request):
        photo = PictureEntry.query(PictureEntry.url == request.url).get()

        if photo is None:
            return GeneralLikeResponse(message="Artwork not found!")

        like = LikeEntry.query(ndb.AND(LikeEntry.url == request.url,
                                       LikeEntry.email == request.email.lower())).get()

        if like is None:  # Aggiungo il like
            photo.likes += 1
            photo.put()
            like = LikeEntry(parent=photo.key, url=request.url, email=request.email.lower())
            like.put()
            status = True
        else:  # Rimuovo il like
            if photo.likes >= 0:  # Per evitare errori strani
                photo.likes -= 1
            photo.put()
            like.key.delete()
            status = False

        return GeneralLikeResponse(hasLiked=status, likes=photo.likes)


    @classmethod
    def login(cls):
        gd_client = gdata.photos.service.PhotosService()
        gd_client.email = 'insert mail'
        gd_client.password = 'insert password'
        gd_client.source = 'art-everywhere'
        gd_client.ProgrammaticLogin()
        return gd_client

APPLICATION = endpoints.api_server([PicasaWA])