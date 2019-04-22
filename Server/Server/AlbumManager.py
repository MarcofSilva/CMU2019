from Album import Album
from UserAlbums import UserAlbums
from flask_sqlalchemy import SQLAlchemy
class AlbumManager:

	def createAlbum(self, user, albumName, albumUrl):
		album = Album(user, albumName, albumUrl)
		print(album)
		print(user)
		userAlbums = user.albums
		if not userAlbums:
			print("No albums yet")
			userAlbums = UserAlbums(user, [album])
			print(userAlbums)
			return userAlbums
		print("already found albums")
		userAlbums.addAlbum(album)
		print(userAlbums)
		return userAlbums

	def addUsersToAlbum(self, user, albumName, usernames, userUrl=None):
		userAlbums = user.albums
		album = self.getAlbum(user, albumName)
		for u in usernames:
			album.addUser(u, userUrl)
		return userAlbums

	def getAlbum(self, user, albumName):
		userAlbums = user.albums
		if not userAlbums:
			print("No albums yet")
			return False
		album = userAlbums.getAlbum(albumName)
		if not album:
			return False
		return album





	


